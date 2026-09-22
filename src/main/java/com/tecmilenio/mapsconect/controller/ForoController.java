package com.tecmilenio.mapsconect.controller;

import com.tecmilenio.mapsconect.exception.ResourceNotFoundException;

import com.tecmilenio.mapsconect.dto.ApiResponse;
import com.tecmilenio.mapsconect.dto.CrearPublicacionDTO;
import com.tecmilenio.mapsconect.dto.CrearRespuestaDTO;
import com.tecmilenio.mapsconect.dto.DuplicadoDTO;
import com.tecmilenio.mapsconect.dto.PublicacionDTO;
import com.tecmilenio.mapsconect.dto.RespuestaForoDTO;
import com.tecmilenio.mapsconect.entity.Usuario;
import com.tecmilenio.mapsconect.repository.PublicacionRepository;
import com.tecmilenio.mapsconect.repository.SeguimientoRepository;
import com.tecmilenio.mapsconect.repository.UsuarioRepository;
import com.tecmilenio.mapsconect.service.CarreraContextoService;
import com.tecmilenio.mapsconect.security.RateLimited;
import com.tecmilenio.mapsconect.service.ForoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador del foro académico (dudas y respuestas).
 *
 * <p>Permite listar publicaciones del foro (filtradas por las materias de la
 * carrera del usuario) y crear nuevas preguntas. El endpoint de creación
 * incluye detección automática de preguntas duplicadas (similarity) y
 * devuelve 409 Conflict si se encuentra una pregunta repetida.</p>
 *
 * <p>Los endpoints de listado y creación están protegidos con
 * {@code @RateLimited} para prevenir spam.</p>
 *
 * @see ForoService
 * @see PublicacionRepository
 */
@Tag(name = "Foro", description = "Dudas y respuestas académicas")
@RestController
@RequestMapping("/foro")
public class ForoController {

    /** Formateador de fechas para la publicación (ej. "5 de enero"). */
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("d 'de' MMMM");

    @Autowired
    private PublicacionRepository publicacionRepository;

    @Autowired
    private SeguimientoRepository seguimientoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ForoService foroService;

    @Autowired
    private CarreraContextoService carreraContextoService;

    /**
     * Lista las publicaciones del foro (máximo 100), filtradas por las
     * materias que pertenecen al plan de estudios de la carrera del usuario.
     *
     * @param authentication usuario autenticado (se usa su carrera para filtrar)
     * @return lista de publicaciones con datos de autor, materia, respuestas y estado
     */
    @Operation(summary = "Listar foro", description = "Devuelve las publicaciones del foro con soporte de paginación, filtradas por materias de tu carrera.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PublicacionDTO>>> listar(
            @org.springframework.web.bind.annotation.RequestParam(value = "page", defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(value = "size", defaultValue = "20") int size,
            Authentication authentication) {

        Usuario yo = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        java.util.Set<Integer> materiasCarrera = carreraContextoService.materiasDeCarrera(authentication.getName());

        int safeSize = Math.max(1, Math.min(size, 100));
        int offset = Math.max(0, page) * safeSize;

        List<PublicacionDTO> publicaciones = publicacionRepository.findForoConDetallesPaginado(safeSize, offset).stream()
                .filter(fila -> materiasCarrera == null
                        || fila[5] == null
                        || materiasCarrera.contains(((Number) fila[5]).intValue()))
                .map(fila -> mapear(fila, yo.getId()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(publicaciones, "Foro obtenido"));
    }

    /**
     * Crea una nueva publicación en el foro.
     *
     * <p>Antes de publicar, el servicio verifica si existe una pregunta
     * similar (detección de duplicados). Si la encuentra, responde con
     * 409 Conflict y devuelve la publicación duplicada como dato.</p>
     *
     * @param request datos de la pregunta (título, contenido, materia, etc.)
     * @param authentication usuario que publica
     * @return 201 con la publicación creada, o 409 si hay duplicado
     */
    @Operation(summary = "Publicar en foro", description = "Crea una nueva publicación. " +
            "Detecta duplicados automáticamente y devuelve 409 si encuentra una pregunta similar.")
    @PostMapping
    @RateLimited(scope = "foro")
    public ResponseEntity<ApiResponse<?>> crear(
            @Valid @RequestBody CrearPublicacionDTO request,
            Authentication authentication) {

        // Detección de preguntas repetidas o muy parecidas.
        var duplicado = foroService.buscarDuplicado(authentication.getName(), request);
        if (duplicado.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.builder()
                            .status(409)
                            .message("Ya existe una pregunta muy parecida en el foro")
                            .data(duplicado.get())
                            .timestamp(java.time.LocalDateTime.now().toString())
                            .build());
        }

        PublicacionDTO creada = foroService.publicar(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(creada, "Duda publicada correctamente"));
    }

    @Operation(summary = "Listar respuestas de una duda", description = "Devuelve todas las respuestas aportadas a una publicación.")
    @GetMapping("/{id}/respuestas")
    public ResponseEntity<ApiResponse<List<RespuestaForoDTO>>> listarRespuestas(
            @PathVariable Integer id) {
        List<RespuestaForoDTO> respuestas = foroService.listarRespuestas(id);
        return ResponseEntity.ok(ApiResponse.success(respuestas, "Respuestas obtenidas"));
    }

    @Operation(summary = "Responder a una duda", description = "Publica una nueva respuesta a una publicación del foro.")
    @PostMapping("/{id}/respuestas")
    @RateLimited(scope = "foro")
    public ResponseEntity<ApiResponse<RespuestaForoDTO>> responder(
            @PathVariable Integer id,
            @Valid @RequestBody CrearRespuestaDTO request,
            Authentication authentication) {
        RespuestaForoDTO creada = foroService.responder(id, authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(creada, "Respuesta publicada correctamente"));
    }

    private PublicacionDTO mapear(Object[] fila, Integer idYo) {
        Integer id = ((Number) fila[0]).intValue();
        String titulo = (String) fila[1];
        String contenido = (String) fila[2];
        Integer idMateria = fila[5] == null ? null : ((Number) fila[5]).intValue();
        String autor = (String) fila[6];
        String materia = (String) fila[7];
        int respuestas = fila[8] == null ? 0 : ((Number) fila[8]).intValue();
        boolean resuelto = esVerdadero(fila[9]);
        String solucion = (String) fila[10];
        Integer autorId = fila[12] == null ? null : ((Number) fila[12]).intValue();
        boolean siguiendo = autorId != null
                && seguimientoRepository.existsByIdSeguidorAndIdSeguido(idYo, autorId);

        String tiempo = aLocalDateTime(fila[4]) != null ? aLocalDateTime(fila[4]).format(FECHA) : "";

        return PublicacionDTO.builder()
                .id(id)
                .titulo(titulo)
                .descripcion(contenido)
                .idMateria(idMateria)
                .materia(materia)
                .autor(autor)
                .tiempo(tiempo)
                .votos(0)
                .respuestas(respuestas)
                .resuelto(resuelto)
                .solucion(solucion)
                .autorId(autorId)
                .autorFoto(fila[13] == null ? null : String.valueOf(fila[13]))
                .siguiendo(siguiendo)
                .semestre(fila[11] == null ? null : String.valueOf(((Number) fila[11]).intValue()))
                .build();
    }

    private static LocalDateTime aLocalDateTime(Object o) {
        if (o instanceof LocalDateTime lt) return lt;
        if (o instanceof java.sql.Timestamp ts) return ts.toLocalDateTime();
        if (o instanceof java.util.Date d) return new java.sql.Timestamp(d.getTime()).toLocalDateTime();
        return null;
    }

    private static boolean esVerdadero(Object o) {
        if (o instanceof Boolean b) return b;
        if (o instanceof Number n) return n.intValue() == 1;
        return false;
    }

}


