package com.tecmilenio.mapsconect.controller;

import com.tecmilenio.mapsconect.exception.ResourceNotFoundException;

import com.tecmilenio.mapsconect.dto.ActualizarNombreDTO;
import com.tecmilenio.mapsconect.dto.ApiResponse;
import com.tecmilenio.mapsconect.dto.CambiarContrasenaDTO;
import com.tecmilenio.mapsconect.dto.FotoRequestDTO;
import com.tecmilenio.mapsconect.entity.Carrera;
import com.tecmilenio.mapsconect.entity.Certificado;
import com.tecmilenio.mapsconect.entity.Estudiante;
import com.tecmilenio.mapsconect.entity.EstudianteCertificado;
import com.tecmilenio.mapsconect.entity.Materia;
import com.tecmilenio.mapsconect.entity.Profesor;
import com.tecmilenio.mapsconect.entity.ProfesorMateria;
import com.tecmilenio.mapsconect.entity.Seguimiento;
import com.tecmilenio.mapsconect.entity.Usuario;
import com.tecmilenio.mapsconect.repository.CarreraRepository;
import com.tecmilenio.mapsconect.repository.CertificadoRepository;
import com.tecmilenio.mapsconect.repository.EstudianteCertificadoRepository;
import com.tecmilenio.mapsconect.repository.EstudianteRepository;
import com.tecmilenio.mapsconect.repository.MateriaRepository;
import com.tecmilenio.mapsconect.repository.ProfesorMateriaRepository;
import com.tecmilenio.mapsconect.repository.ProfesorRepository;
import com.tecmilenio.mapsconect.repository.PublicacionRepository;
import com.tecmilenio.mapsconect.repository.SeguimientoRepository;
import com.tecmilenio.mapsconect.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador de gestión de usuarios y perfil.
 *
 * <p>Expone endpoints para consultar perfiles públicos de otros usuarios,
 * gestionar la cuenta (cambio de nombre, contraseña, foto), seguir/dejar
 * de seguir usuarios, y buscar/descubrir personas en la plataforma.</p>
 *
 * <p>Todos los endpoints requieren autenticación JWT. La identidad del
 * usuario se obtiene a través del parámetro {@link Authentication},
 * resuelto automáticamente por Spring Security.</p>
 *
 * <p>Para estudiantes: se muestra semestre, carrera, certificados, propósito
 * de vida, y aportes (dudas y respuestas) recientes.</p>
 * <p>Para profesores: se muestra especialidad, biografía, horario de asesorías,
 * disponibilidad de chat, materias impartidas, y respuestas recientes.</p>
 */
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SeguimientoRepository seguimientoRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private CarreraRepository carreraRepository;

    @Autowired
    private PublicacionRepository publicacionRepository;

    @Autowired
    private CertificadoRepository certificadoRepository;

    @Autowired
    private EstudianteCertificadoRepository estudianteCertificadoRepository;

    @Autowired
    private ProfesorRepository profesorRepository;

    @Autowired
    private ProfesorMateriaRepository profesorMateriaRepository;

    @Autowired
    private MateriaRepository materiaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String FECHA_FORMATO = "dd/MM/yyyy";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern(FECHA_FORMATO);

    // ---- Perfil público de otro usuario ----

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> perfilPublico(
            @PathVariable Integer id, Authentication authentication) {

        Usuario yo = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Usuario target = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Map<String, Object> perfil = new LinkedHashMap<>();
        perfil.put("id", target.getId());
        perfil.put("nombre", target.getNombreCompleto());
        perfil.put("foto", target.getFotoUrl());
        perfil.put("rol", target.getRol().name());
        perfil.put("esYo", target.getId().equals(yo.getId()));
        perfil.put("reputacion", target.getPuntosReputacion());
        perfil.put("seguidores", seguimientoRepository.countByIdSeguido(target.getId()));
        perfil.put("siguiendo", seguimientoRepository.countByIdSeguidor(target.getId()));
        perfil.put("loSigo", seguimientoRepository.existsByIdSeguidorAndIdSeguido(yo.getId(), target.getId()));

        if (target.getRol() == Usuario.Rol.ESTUDIANTE) {
            perfil.putAll(perfilEstudiante(target));
        } else if (target.getRol() == Usuario.Rol.PROFESOR) {
            perfil.putAll(perfilProfesor(target));
        } else {
            perfil.put("carrera", null);
            perfil.put("semestre", null);
            perfil.put("proposito", null);
            perfil.put("certificados", List.of());
            perfil.put("especialidad", null);
            perfil.put("materias", List.of());
            perfil.put("aportes", List.of());
            perfil.put("dudas", 0L);
            perfil.put("respuestas", 0L);
        }

        return ResponseEntity.ok(ApiResponse.success(perfil, "Perfil obtenido"));
    }

    private Map<String, Object> perfilEstudiante(Usuario target) {
        Map<String, Object> dato = new LinkedHashMap<>();
        Estudiante est = estudianteRepository.findByIdUsuario(target.getId()).orElse(null);
        Integer idEstudiante = null;
        if (est != null) {
            idEstudiante = est.getId();
            dato.put("semestre", est.getSemestreActual());
            dato.put("proposito", est.getPropositoVida());
            dato.put("carrera", est.getIdCarrera() == null ? null
                    : carreraRepository.findById(est.getIdCarrera()).map(Carrera::getNombre).orElse(null));
        } else {
            dato.put("semestre", null);
            dato.put("proposito", null);
            dato.put("carrera", null);
        }

        List<Map<String, Object>> certificados = new ArrayList<>();
        if (est != null) {
            for (EstudianteCertificado asig : estudianteCertificadoRepository
                    .findByIdEstudianteOrderByFechaSeleccionAsc(est.getId())) {
                Certificado cert = certificadoRepository.findById(asig.getIdCertificado()).orElse(null);
                if (cert != null) {
                    Map<String, Object> c = new LinkedHashMap<>();
                    c.put("titulo", cert.getNombre());
                    c.put("descripcion", cert.getDescripcion());
                    certificados.add(c);
                }
            }
        }
        dato.put("certificados", certificados);
        dato.put("especialidad", null);
        dato.put("materias", List.of());

        long dudas = idEstudiante != null ? publicacionRepository.countByIdEstudiante(idEstudiante) : 0L;
        long respuestas = publicacionRepository.countRespuestasDeUsuario(target.getId());
        dato.put("dudas", dudas);
        dato.put("respuestas", respuestas);
        dato.put("aportes", aportesEstudiante(idEstudiante));
        return dato;
    }

    private Map<String, Object> perfilProfesor(Usuario target) {
        Map<String, Object> dato = new LinkedHashMap<>();
        dato.put("carrera", null);
        dato.put("semestre", null);
        dato.put("proposito", null);
        dato.put("certificados", List.of());
        dato.put("dudas", 0L);
        dato.put("respuestas", publicacionRepository.countRespuestasDeUsuario(target.getId()));

        Profesor prof = profesorRepository.findByIdUsuario(target.getId()).orElse(null);
        if (prof != null) {
            dato.put("especialidad", prof.getAreaEspecialidad());
            dato.put("biografia", prof.getBiografia());
            dato.put("horarioAsesorias", prof.getHorarioAsesorias());
            dato.put("disponibleChat", prof.getDisponibleChat());
            List<String> materias = new ArrayList<>();
            for (ProfesorMateria pm : profesorMateriaRepository.findByIdProfesor(prof.getId())) {
                materiaRepository.findById(pm.getIdMateria())
                        .map(Materia::getNombre)
                        .ifPresent(materias::add);
            }
            dato.put("materias", materias);
        } else {
            dato.put("especialidad", null);
            dato.put("biografia", null);
            dato.put("horarioAsesorias", null);
            dato.put("disponibleChat", false);
            dato.put("materias", List.of());
        }

        dato.put("aportes", aportesProfesor(target.getId()));
        return dato;
    }

    private List<Map<String, Object>> aportesEstudiante(Integer idEstudiante) {
        List<Map<String, Object>> aportes = new ArrayList<>();
        if (idEstudiante == null) return aportes;
        for (Object[] fila : publicacionRepository.findAportesRecientesDeEstudiante(idEstudiante, 3)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("titulo", fila[0]);
            item.put("detalle", fila[1]);
            item.put("tipo", "duda");
            String fecha = "";
            if (fila[2] != null) {
                java.sql.Timestamp ts = (java.sql.Timestamp) fila[2];
                fecha = ts.toLocalDateTime().format(FORMATO_FECHA);
            }
            item.put("meta", fecha + (fila[3] != null ? " • " + fila[3] + " respuestas" : ""));
            aportes.add(item);
        }
        return aportes;
    }

    private List<Map<String, Object>> aportesProfesor(Integer idUsuario) {
        List<Map<String, Object>> aportes = new ArrayList<>();
        for (Object[] fila : publicacionRepository.findUltimasRespuestasDeUsuario(idUsuario, 3)) {
            Map<String, Object> item = new LinkedHashMap<>();
            String tituloDuda = fila[3] == null ? "Duda en el foro" : String.valueOf(fila[3]);
            item.put("titulo", "Respondió en: " + tituloDuda);
            item.put("detalle", fila[1]);
            item.put("tipo", "respuesta");
            boolean esSolucion = fila[2] != null && ((Number) fila[2]).intValue() == 1;
            item.put("meta", esSolucion ? "Solución aportada" : "Respuesta aportada");
            aportes.add(item);
        }
        return aportes;
    }

    // ---- Datos de la cuenta (Ajustes) ----

    @PutMapping("/cuenta/nombre")
    public ResponseEntity<ApiResponse<Map<String, Object>>> actualizarNombre(
            @Valid @RequestBody ActualizarNombreDTO request, Authentication authentication) {

        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        String nombre = request.getNombre().trim();
        if (nombre.isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }

        usuario.setNombreCompleto(nombre);
        usuarioRepository.save(usuario);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", usuario.getId());
        data.put("email", usuario.getEmail());
        data.put("nombre", usuario.getNombreCompleto());
        data.put("rol", usuario.getRol().name());
        data.put("activo", usuario.getActivo());
        data.put("foto", usuario.getFotoUrl());
        return ResponseEntity.ok(ApiResponse.success(data, "Nombre actualizado correctamente"));
    }

    @PutMapping("/cuenta/contrasena")
    public ResponseEntity<ApiResponse<Map<String, Object>>> cambiarContrasena(
            @Valid @RequestBody CambiarContrasenaDTO request, Authentication authentication) {

        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getContrasenaActual(), usuario.getContrasena())) {
            throw new IllegalArgumentException("La contraseña actual no es correcta");
        }

        if (passwordEncoder.matches(request.getContrasenaNueva(), usuario.getContrasena())) {
            throw new IllegalArgumentException("La nueva contraseña no puede ser igual a la actual");
        }

        usuario.setContrasena(passwordEncoder.encode(request.getContrasenaNueva()));
        usuarioRepository.save(usuario);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("actualizado", Boolean.TRUE);
        return ResponseEntity.ok(ApiResponse.success(data, "Contraseña actualizada correctamente"));
    }

    // ---- Foto de perfil ----

    @PutMapping("/foto")
    public ResponseEntity<ApiResponse<Map<String, Object>>> actualizarFoto(
            @Valid @RequestBody FotoRequestDTO request, Authentication authentication) {

        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        String foto = request.getFoto();
        if (foto != null && !foto.isBlank()) {
            if (!foto.startsWith("data:image/")) {
                throw new IllegalArgumentException("La foto debe ser una imagen en formato data URI");
            }
            String formato = foto.toLowerCase(java.util.Locale.ROOT);
            boolean permitida = formato.startsWith("data:image/png;")
                    || formato.startsWith("data:image/jpeg;")
                    || formato.startsWith("data:image/webp;");
            if (!permitida) {
                throw new IllegalArgumentException(
                        "Formato no permitido. Usa PNG, JPG o WebP (se rechazan SVG y otros formatos).");
            }
            long maxBase64 = 2L * 1024 * 1024 * 4 / 3 + 128;
            if (foto.length() > maxBase64) {
                throw new IllegalArgumentException("La imagen no puede superar 2 MB");
            }
        }

        usuario.setFotoUrl(foto == null || foto.isBlank() ? null : foto);
        usuarioRepository.save(usuario);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("foto", usuario.getFotoUrl());
        return ResponseEntity.ok(ApiResponse.success(data, "Foto de perfil actualizada"));
    }

    @DeleteMapping("/foto")
    public ResponseEntity<ApiResponse<Map<String, Object>>> eliminarFoto(Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        usuario.setFotoUrl(null);
        usuarioRepository.save(usuario);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("foto", null);
        return ResponseEntity.ok(ApiResponse.success(data, "Foto de perfil eliminada"));
    }

    // ---- Seguir / dejar de seguir ----

    @PostMapping("/{id}/seguir")
    public ResponseEntity<ApiResponse<Map<String, Object>>> seguir(
            @PathVariable Integer id, Authentication authentication) {

        Usuario yo = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (yo.getId().equals(id)) {
            throw new IllegalArgumentException("No puedes seguirte a ti mismo");
        }
        Usuario seguido = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!seguimientoRepository.existsByIdSeguidorAndIdSeguido(yo.getId(), id)) {
            seguimientoRepository.save(Seguimiento.builder()
                    .idSeguidor(yo.getId())
                    .idSeguido(seguido.getId())
                    .build());
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("siguiendo", Boolean.TRUE);
        data.put("seguidores", seguimientoRepository.countByIdSeguido(id));
        return ResponseEntity.ok(ApiResponse.success(data, "Ahora sigues a " + seguido.getNombreCompleto()));
    }

    @DeleteMapping("/{id}/seguir")
    public ResponseEntity<ApiResponse<Map<String, Object>>> dejarDeSeguir(
            @PathVariable Integer id, Authentication authentication) {

        Usuario yo = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (seguimientoRepository.existsByIdSeguidorAndIdSeguido(yo.getId(), id)) {
            seguimientoRepository.deleteByIdSeguidorAndIdSeguido(yo.getId(), id);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("siguiendo", Boolean.FALSE);
        data.put("seguidores", seguimientoRepository.countByIdSeguido(id));
        return ResponseEntity.ok(ApiResponse.success(data, "Dejaste de seguir a este usuario"));
    }

    // ---- Buscador global de personas ----

    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> buscar(
            @RequestParam(value = "q", required = false) String q) {

        List<Map<String, Object>> lista = new ArrayList<>();
        String criterio = q == null ? "" : q.trim();
        if (criterio.length() < 2) {
            return ResponseEntity.ok(ApiResponse.success(lista, "Escribe al menos 2 caracteres"));
        }

        for (Usuario u : usuarioRepository
                .findTop8ByActivoTrueAndNombreCompletoContainingIgnoreCaseOrderByPuntosReputacionDesc(criterio)) {
            Map<String, Object> persona = new LinkedHashMap<>();
            persona.put("id", u.getId());
            persona.put("nombre", u.getNombreCompleto());
            persona.put("foto", u.getFotoUrl());
            persona.put("rol", u.getRol().name());

            String carrera = null;
            Integer semestre = null;
            if (u.getRol() == Usuario.Rol.ESTUDIANTE) {
                Estudiante est = estudianteRepository.findByIdUsuario(u.getId()).orElse(null);
                if (est != null) {
                    semestre = est.getSemestreActual();
                    if (est.getIdCarrera() != null) {
                        carrera = carreraRepository.findById(est.getIdCarrera())
                                .map(Carrera::getNombre).orElse(null);
                    }
                }
            }
            persona.put("carrera", carrera);
            persona.put("semestre", semestre);
            lista.add(persona);
        }

        return ResponseEntity.ok(ApiResponse.success(lista, "Resultados de búsqueda"));
    }

    // ---- Descubrir personas ----

    @GetMapping("/descubrir")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> descubrir(Authentication authentication) {
        Usuario yo = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        List<Map<String, Object>> lista = new ArrayList<>();
        for (Usuario u : usuarioRepository.findAllByActivoTrueOrderByPuntosReputacionDesc()) {
            if (u.getId().equals(yo.getId())) continue;

            Map<String, Object> persona = new LinkedHashMap<>();
            persona.put("id", u.getId());
            persona.put("nombre", u.getNombreCompleto());
            persona.put("foto", u.getFotoUrl());
            persona.put("rol", u.getRol().name());

            String carrera = null;
            if (u.getRol() == Usuario.Rol.ESTUDIANTE) {
                carrera = estudianteRepository.findByIdUsuario(u.getId())
                        .flatMap(e -> e.getIdCarrera() == null
                                ? java.util.Optional.<Carrera>empty()
                                : carreraRepository.findById(e.getIdCarrera()))
                        .map(Carrera::getNombre)
                        .orElse(null);
            }
            persona.put("carrera", carrera);
            persona.put("seguidores", seguimientoRepository.countByIdSeguido(u.getId()));
            persona.put("loSigo", seguimientoRepository.existsByIdSeguidorAndIdSeguido(yo.getId(), u.getId()));
            lista.add(persona);
        }

        return ResponseEntity.ok(ApiResponse.success(lista, "Personas para descubrir"));
    }

}

