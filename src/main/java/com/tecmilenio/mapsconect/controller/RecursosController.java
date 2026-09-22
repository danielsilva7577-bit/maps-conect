package com.tecmilenio.mapsconect.controller;

import com.tecmilenio.mapsconect.dto.ApiResponse;
import com.tecmilenio.mapsconect.dto.CrearRecursoDTO;
import com.tecmilenio.mapsconect.dto.RecursoDTO;
import com.tecmilenio.mapsconect.entity.Materia;
import com.tecmilenio.mapsconect.entity.RecursoAcademico;
import com.tecmilenio.mapsconect.entity.Usuario;
import com.tecmilenio.mapsconect.exception.ResourceNotFoundException;
import com.tecmilenio.mapsconect.repository.MateriaRepository;
import com.tecmilenio.mapsconect.repository.RecursoAcademicoRepository;
import com.tecmilenio.mapsconect.repository.UsuarioRepository;
import com.tecmilenio.mapsconect.security.RateLimited;
import com.tecmilenio.mapsconect.service.CarreraContextoService;
import com.tecmilenio.mapsconect.util.ArchivoSeguro;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controlador de recursos académicos compartidos.
 *
 * <p>Permite listar apuntes y material de estudio, crear recursos nuevos
 * (ya sea mediante URL o subiendo un archivo), y descargar archivos internos.
 * Los recursos pueden estar asociados a una materia específica del plan
 * de estudios del usuario.</p>
 *
 * <p>Validaciones de seguridad:</p>
 * <ul>
 *   <li>Los archivos subidos se validan con {@link com.tecmilenio.mapsconect.util.ArchivoSeguro}
 *       (tipo, extensión y tamaño máximo de 20 MB).</li>
 *   <li>Los estudiantes solo pueden crear recursos para materias de su carrera.</li>
 *   <li>Los archivos internos se almacenan en disco y su URL interna usa el prefijo
 *       {@code "archivo:"} seguido del ID del recurso.</li>
 * </ul>
 *
 * <p>Los endpoints de creación y upload están protegidos con
 * {@code @RateLimited} (scopes: {@code recursos}, {@code uploads}).</p>
 *
 * @see com.tecmilenio.mapsconect.util.ArchivoSeguro
 */
@Tag(name = "Recursos", description = "Apuntes y material académico compartido")
@RestController
@RequestMapping("/recursos")
public class RecursosController {

    private static final String PREFIJO_INTERNO = "archivo:";

    @Autowired
    private RecursoAcademicoRepository recursoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MateriaRepository materiaRepository;

    @Autowired
    private CarreraContextoService carreraContextoService;

    /**
     * Lista recursos académicos visibles, filtrados por las materias del
     * plan de estudios del usuario autenticado.
     *
     * @param authentication usuario autenticado (puede ser nulo)
     * @return 200 OK con la lista de recursos
     */
    @Operation(summary = "Listar recursos", description = "Devuelve apuntes y material académico visibles según las materias de tu carrera.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<RecursoDTO>>> listar(Authentication authentication) {
        Set<Integer> materiasCarrera = authentication == null
                ? null
                : carreraContextoService.materiasDeCarrera(authentication.getName());

        List<RecursoDTO> recursos = recursoRepository.findRecursosVisibles().stream()
                .filter(fila -> materiasCarrera == null
                        || fila.length <= 11
                        || fila[11] == null
                        || materiasCarrera.contains(((Number) fila[11]).intValue()))
                .map(this::mapear)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(recursos, "Recursos obtenidos"));
    }

    /**
     * Crea un recurso académico a partir de una URL de descarga.
     *
     * @param request datos del recurso (título, descripción, URL, materia)
     * @param authentication usuario que crea el recurso
     * @return 201 Created con el recurso creado
     */
    @Operation(summary = "Crear recurso (JSON)", description = "Crea un recurso académico con URL de descarga.")
    @PostMapping
    @RateLimited(scope = "recursos")
    public ResponseEntity<ApiResponse<RecursoDTO>> crear(
            @Valid @RequestBody CrearRecursoDTO request,
            Authentication authentication) {

        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        validarMateriaDeCarrera(authentication, request.getIdMateria());

        String tipo = request.getTipo() == null || request.getTipo().isBlank()
                ? "PDF" : request.getTipo().trim().toUpperCase();

        RecursoAcademico recurso = recursoRepository.save(RecursoAcademico.builder()
                .idUsuario(usuario.getId())
                .idMateria(request.getIdMateria())
                .titulo(request.getTitulo().trim())
                .descripcion(request.getDescripcion())
                .urlArchivo(request.getUrl().trim())
                .tipoArchivo(tipo)
                .fechaSubida(LocalDateTime.now())
                .contadorDescargas(0)
                .contadorReportes(0)
                .oculto(Boolean.FALSE)
                .build());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(mapearDesdeEntidad(recurso, usuario), "Apunte publicado correctamente"));
    }

    @Operation(summary = "Subir archivo", description = "Sube un archivo (max 20MB) como apunte académico. Extensiones permitidas: pdf, doc, xls, ppt, txt, imágenes, zip, audio y video.")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RateLimited(scope = "uploads")
    public ResponseEntity<ApiResponse<RecursoDTO>> subir(
            @RequestParam("titulo") String titulo,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestParam("idMateria") Integer idMateria,
            @RequestParam(value = "tipo", required = false) String tipo,
            @RequestParam(value = "url", required = false) String url,
            @RequestParam(value = "archivo", required = false) MultipartFile archivo,
            Authentication authentication) {

        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("El título del apunte es obligatorio");
        }
        if (titulo.length() > 200) {
            throw new IllegalArgumentException("El título no puede superar los 200 caracteres");
        }
        if (idMateria == null) {
            throw new IllegalArgumentException("Selecciona una materia para el apunte");
        }
        if ((archivo == null || archivo.isEmpty()) && (url == null || url.isBlank())) {
            throw new IllegalArgumentException("Selecciona un archivo o agrega una URL de descarga");
        }

        validarMateriaDeCarrera(authentication, idMateria);

        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        RecursoAcademico recurso;
        if (archivo != null && !archivo.isEmpty()) {
            ArchivoSeguro.validar(archivo.getOriginalFilename(), archivo.getSize());
            String nombreLimpio = ArchivoSeguro.limpiarNombre(archivo.getOriginalFilename());

            RecursoAcademico guardado = recursoRepository.save(RecursoAcademico.builder()
                    .idUsuario(usuario.getId())
                    .idMateria(idMateria)
                    .titulo(titulo.trim())
                    .descripcion(descripcion)
                    .urlArchivo(PREFIJO_INTERNO)
                    .adjuntoNombre(nombreLimpio)
                    .adjuntoTamano(archivo.getSize())
                    .tipoArchivo(tipoDesdeNombre(nombreLimpio))
                    .fechaSubida(LocalDateTime.now())
                    .contadorDescargas(0)
                    .contadorReportes(0)
                    .oculto(Boolean.FALSE)
                    .build());

            // El nombre en disco usa el id del recurso (sin extensión), evita colisiones.
            String rutaFinal = PREFIJO_INTERNO + guardado.getId();
            guardado.setUrlArchivo(rutaFinal);
            recursoRepository.save(guardado);

            try {
                Path destino = carpetaRecursos().resolve(String.valueOf(guardado.getId()));
                Files.createDirectories(destino.getParent());
                archivo.transferTo(destino);
            } catch (IOException e) {
                throw new IllegalArgumentException("No se pudo guardar el archivo del apunte");
            }

            recurso = guardado;
        } else {
            String tipoLink = tipo == null || tipo.isBlank() ? "LINK" : tipo.trim().toUpperCase();
            recurso = recursoRepository.save(RecursoAcademico.builder()
                    .idUsuario(usuario.getId())
                    .idMateria(idMateria)
                    .titulo(titulo.trim())
                    .descripcion(descripcion)
                    .urlArchivo(url.trim())
                    .tipoArchivo(tipoLink)
                    .fechaSubida(LocalDateTime.now())
                    .contadorDescargas(0)
                    .contadorReportes(0)
                    .oculto(Boolean.FALSE)
                    .build());
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(mapearDesdeEntidad(recurso, usuario), "Apunte publicado correctamente"));
    }

    @Operation(summary = "Descargar archivo", description = "Descarga el archivo interno de un apunte (requiere que el recurso tenga archivo interno).")
    @GetMapping("/{id}/archivo")
    public ResponseEntity<byte[]> descargarArchivo(@PathVariable Integer id) {
        RecursoAcademico recurso = recursoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Apunte no encontrado"));

        if (Boolean.TRUE.equals(recurso.getOculto())
                || recurso.getUrlArchivo() == null
                || !recurso.getUrlArchivo().startsWith(PREFIJO_INTERNO)) {
            throw new ResourceNotFoundException("Este apunte no tiene archivo interno");
        }

        try {
            Path origen = carpetaRecursos().resolve(String.valueOf(recurso.getId()));
            byte[] bytes = Files.readAllBytes(origen);

            if (recurso.getContadorDescargas() != null) {
                recurso.setContadorDescargas(recurso.getContadorDescargas() + 1);
                recursoRepository.save(recurso);
            }

            String nombreDescarga = recurso.getAdjuntoNombre() != null
                    ? recurso.getAdjuntoNombre() : ("recurso-" + recurso.getId());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, ArchivoSeguro.encabezadoDescargaSeguro(nombreDescarga))
                    .header(HttpHeaders.CONTENT_TYPE, mimeDesdeExtension(nombreDescarga))
                    .contentLength(bytes.length)
                    .body(bytes);
        } catch (IOException e) {
            throw new ResourceNotFoundException("El archivo del apunte no está disponible");
        }
    }

    private RecursoDTO mapear(Object[] fila) {
        Integer id = ((Number) fila[0]).intValue();
        String titulo = (String) fila[1];
        String descripcion = (String) fila[2];
        String url = (String) fila[3];
        String tipo = (String) fila[4];
        int descargas = fila[6] == null ? 0 : ((Number) fila[6]).intValue();
        String autor = (String) fila[7];
        String materia = (String) fila[8];
        String adjuntoNombre = fila[9] == null ? null : (String) fila[9];
        Long adjuntoTamano = fila[10] == null ? null : ((Number) fila[10]).longValue();

        return RecursoDTO.builder()
                .id(id)
                .titulo(titulo)
                .descripcion(descripcion)
                .materia(materia)
                .autor(autor)
                .tipo(tipo == null ? "PDF" : tipo)
                .url(url)
                .descargas(descargas)
                .tiempo("")
                .interno(url != null && url.startsWith(PREFIJO_INTERNO))
                .adjuntoNombre(adjuntoNombre)
                .adjuntoTamano(adjuntoTamano)
                .build();
    }

    private RecursoDTO mapearDesdeEntidad(RecursoAcademico recurso, Usuario usuario) {
        String materia = materiaRepository.findById(recurso.getIdMateria())
                .map(Materia::getNombre).orElse(null);

        return RecursoDTO.builder()
                .id(recurso.getId())
                .titulo(recurso.getTitulo())
                .descripcion(recurso.getDescripcion())
                .materia(materia)
                .autor(usuario.getNombreCompleto())
                .tipo(recurso.getTipoArchivo() == null ? "PDF" : recurso.getTipoArchivo())
                .url(recurso.getUrlArchivo())
                .descargas(recurso.getContadorDescargas() == null ? 0 : recurso.getContadorDescargas())
                .tiempo("")
                .interno(recurso.getUrlArchivo() != null && recurso.getUrlArchivo().startsWith(PREFIJO_INTERNO))
                .adjuntoNombre(recurso.getAdjuntoNombre())
                .adjuntoTamano(recurso.getAdjuntoTamano())
                .build();
    }

    private String tipoDesdeNombre(String nombre) {
        String ext = extension(nombre);
        switch (ext) {
            case "pdf": return "PDF";
            case "doc":
            case "docx": return "DOC";
            case "ppt":
            case "pptx": return "PPT";
            case "xls":
            case "xlsx":
            case "csv": return "XLS";
            case "txt": return "TXT";
            case "zip":
            case "rar":
            case "7z": return "ZIP";
            case "png":
            case "jpg":
            case "jpeg":
            case "gif":
            case "webp": return "IMG";
            default: return "PDF";
        }
    }

    private String mimeDesdeExtension(String nombre) {
        String ext = extension(nombre);
        switch (ext) {
            case "pdf": return MediaType.APPLICATION_PDF_VALUE;
            case "doc": return "application/msword";
            case "docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "ppt": return "application/vnd.ms-powerpoint";
            case "pptx": return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "xls": return "application/vnd.ms-excel";
            case "xlsx": return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "csv": return "text/csv";
            case "txt": return "text/plain";
            case "zip": return "application/zip";
            case "png": return "image/png";
            case "jpg":
            case "jpeg": return "image/jpeg";
            case "gif": return "image/gif";
            case "webp": return "image/webp";
            default: return "application/octet-stream";
        }
    }

    private String extension(String nombre) {
        if (nombre == null) return "";
        int idx = nombre.lastIndexOf('.');
        return idx >= 0 ? nombre.substring(idx + 1).toLowerCase() : "";
    }

    private Path carpetaRecursos() {
        return Paths.get(System.getProperty("user.dir"), "uploads", "recursos").toAbsolutePath().normalize();
    }

    private void validarMateriaDeCarrera(Authentication authentication, Integer idMateria) {
        if (authentication == null || idMateria == null) {
            return;
        }
        if (carreraContextoService.esEstudianteConCarrera(authentication.getName())
                && !carreraContextoService.materiaEsDeCarrera(authentication.getName(), idMateria)) {
            throw new IllegalArgumentException("La materia no pertenece al plan de tu carrera");
        }
    }
}