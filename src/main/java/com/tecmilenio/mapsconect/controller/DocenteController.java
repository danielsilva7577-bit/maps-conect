package com.tecmilenio.mapsconect.controller;

import com.tecmilenio.mapsconect.dto.ApiResponse;
import com.tecmilenio.mapsconect.entity.Publicacion;
import com.tecmilenio.mapsconect.entity.Profesor;
import com.tecmilenio.mapsconect.entity.Usuario;
import com.tecmilenio.mapsconect.repository.EstudianteRepository;
import com.tecmilenio.mapsconect.repository.ProfesorRepository;
import com.tecmilenio.mapsconect.repository.PublicacionRepository;
import com.tecmilenio.mapsconect.repository.RecursoAcademicoRepository;
import com.tecmilenio.mapsconect.repository.SesionRepasoRepository;
import com.tecmilenio.mapsconect.repository.UsuarioRepository;
import com.tecmilenio.mapsconect.service.NotificacionService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador de funcionalidades exclusivas para docentes.
 *
 * <p>Proporciona endpoints para consultar estadísticas del panel docente,
 * validar y cerrar dudas del foro, y programar asesorías individuales o
 * grupales mediante sesiones de repaso.</p>
 *
 * <p>Todos los endpoints verifican que el usuario autenticado tenga el
 * rol {@code PROFESOR} antes de procesar la solicitud.</p>
 */
@RestController
@RequestMapping("/docente")
public class DocenteController {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ProfesorRepository profesorRepository;
    @Autowired
    private PublicacionRepository publicacionRepository;
    @Autowired
    private SesionRepasoRepository sesionRepasoRepository;
    @Autowired
    private EstudianteRepository estudianteRepository;
    @Autowired
    private RecursoAcademicoRepository recursoRepository;
    @Autowired
    private NotificacionService notificacionService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> stats(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "No autenticado"));
        }

        Usuario usuario = usuarioRepository.findByEmail(authentication.getName()).orElse(null);
        if (usuario == null || usuario.getRol() != Usuario.Rol.PROFESOR) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), "Se requiere rol de profesor"));
        }

        Profesor profesor = profesorRepository.findByIdUsuario(usuario.getId()).orElse(null);
        if (profesor == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), "Perfil de profesor incompleto"));
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("perfil", perfil(usuario, profesor));
        data.put("kpis", kpis(usuario.getId(), profesor));
        data.put("dudas", dudasAbiertas());
        data.put("asesorias", asesorias(usuario.getId()));

        return ResponseEntity.ok(ApiResponse.success(data, "Datos del panel docente"));
    }

    @PostMapping("/dudas/{id}/validar")
    public ResponseEntity<ApiResponse<?>> validarDuda(
            @PathVariable Integer id, Authentication authentication) {
        if (!esProfesor(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), "Se requiere rol de profesor"));
        }

        Publicacion publicacion = publicacionRepository.findById(id).orElse(null);
        if (publicacion == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), "Duda no encontrada"));
        }
        if ("resuelta".equalsIgnoreCase(publicacion.getEstado())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(HttpStatus.CONFLICT.value(), "La duda ya estaba resuelta"));
        }

        publicacion.setEstado("resuelta");
        publicacionRepository.save(publicacion);

        notificarAlumnoDudaResuelta(publicacion);

        return ResponseEntity.ok(ApiResponse.success(null, "Duda validada y cerrada"));
    }

    private void notificarAlumnoDudaResuelta(Publicacion publicacion) {
        estudianteRepository.findById(publicacion.getIdEstudiante())
                .ifPresent(estudiante -> notificacionService.notificar(
                        estudiante.getIdUsuario(),
                        "duda",
                        "Tu duda fue resuelta",
                        publicacion.getTitulo() != null ? publicacion.getTitulo() : "Duda del foro",
                        "comunidad.html",
                        publicacion.getId()));
    }

    @PostMapping("/asesorias")
    public ResponseEntity<ApiResponse<?>> crearAsesoria(
            @RequestBody Map<String, Object> body, Authentication authentication) {
        if (!esProfesor(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), "Se requiere rol de profesor"));
        }

        String titulo = (String) body.get("titulo");
        String materia = (String) body.get("materia");
        String fechaStr = (String) body.get("fecha");
        String horaStr = (String) body.get("hora");

        if (titulo == null || titulo.isBlank() || materia == null || materia.isBlank()
                || fechaStr == null || horaStr == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "titulo, materia, fecha y hora son obligatorios"));
        }

        Usuario usuario = usuarioRepository.findByEmail(authentication.getName()).orElse(null);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), "Usuario no encontrado"));
        }

        java.sql.Date fecha;
        java.sql.Time hora;
        try {
            fecha = java.sql.Date.valueOf(java.time.LocalDate.parse(fechaStr));
            hora = java.sql.Time.valueOf(java.time.LocalTime.parse(horaStr));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "fecha debe ser AAAA-MM-DD y hora HH:MM"));
        }

        com.tecmilenio.mapsconect.entity.SesionRepaso sesion = com.tecmilenio.mapsconect.entity.SesionRepaso.builder()
                .titulo(titulo.trim())
                .descripcion((String) body.get("descripcion"))
                .materia(materia.trim())
                .modalidad(body.get("modalidad") == null || String.valueOf(body.get("modalidad")).isBlank()
                        ? "Grupal" : String.valueOf(body.get("modalidad")).trim())
                .ubicacion((String) body.get("ubicacion"))
                .fecha(fecha.toLocalDate())
                .horaInicio(hora.toLocalTime())
                .duracionMin(body.get("duracionMin") == null ? 90 : ((Number) body.get("duracionMin")).intValue())
                .cupoMax(body.get("cupoMax") == null ? 30 : ((Number) body.get("cupoMax")).intValue())
                .estado("ABIERTA")
                .organizadorId(usuario.getId())
                .creadoEn(java.time.LocalDateTime.now())
                .build();

        com.tecmilenio.mapsconect.entity.SesionRepaso guardada = sesionRepasoRepository.save(sesion);

        notificarAsesoriaNueva(usuario, guardada);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(Map.of("id", guardada.getId()), "Asesoría programada"));
    }

    private void notificarAsesoriaNueva(Usuario docente, com.tecmilenio.mapsconect.entity.SesionRepaso sesion) {
        String titulo = "Nueva asesoría de " + docente.getNombreCompleto();
        String preview = (sesion.getMateria() != null ? "[" + sesion.getMateria() + "] " : "")
                + (sesion.getTitulo() != null ? sesion.getTitulo() : "Sesión de repaso");
        for (Usuario estudiante : usuarioRepository.findByRolAndActivoTrue(Usuario.Rol.ESTUDIANTE)) {
            notificacionService.notificar(estudiante.getId(), "asesoria", titulo, preview,
                    "circulos.html", sesion.getId());
        }
    }

    private boolean esProfesor(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) return false;
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName()).orElse(null);
        return usuario != null && usuario.getRol() == Usuario.Rol.PROFESOR;
    }

    private Map<String, Object> perfil(Usuario usuario, Profesor profesor) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("nombre", usuario.getNombreCompleto());
        p.put("nomina", profesor.getNumeroNomina());
        p.put("area", profesor.getAreaEspecialidad());
        p.put("horario", profesor.getHorarioAsesorias());
        p.put("enlace", profesor.getEnlaceSalaVirtual());
        p.put("rol", "Asesor Docente");
        return p;
    }

    private Map<String, Object> kpis(Integer idUsuario, Profesor profesor) {
        long dudasPorValidar = publicacionRepository.countByEstadoIgnoreCase("abierta");
        long circulosACargo = sesionRepasoRepository.findSesionesDeOrganizador(idUsuario).size();
        long alumnosAsesorados = estudianteRepository.count();
        long descargasApuntes = recursoRepository.sumDescargasDeUsuario(idUsuario);

        Map<String, Object> kpis = new LinkedHashMap<>();
        kpis.put("dudasPorValidar", dudasPorValidar);
        kpis.put("circulosACargo", circulosACargo);
        kpis.put("alumnosAsesorados", alumnosAsesorados);
        kpis.put("descargasApuntes", descargasApuntes);
        return kpis;
    }

    private List<Map<String, Object>> dudasAbiertas() {
        List<Map<String, Object>> lista = new ArrayList<>();
        for (Object[] fila : publicacionRepository.findDudasAbiertasConDetalles()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", ((Number) fila[0]).intValue());
            item.put("titulo", (String) fila[1]);
            item.put("contenido", (String) fila[2]);
            item.put("materia", fila[5] == null ? "General" : (String) fila[5]);
            item.put("autor", (String) fila[4]);
            lista.add(item);
        }
        return lista;
    }

    private List<Map<String, Object>> asesorias(Integer idUsuario) {
        List<Map<String, Object>> lista = new ArrayList<>();
        DateTimeFormatter fechaFmt = DateTimeFormatter.ofPattern("EEE d 'de' MMMM");
        DateTimeFormatter horaFmt = DateTimeFormatter.ofPattern("HH:mm");
        for (Object[] fila : sesionRepasoRepository.findSesionesDeOrganizador(idUsuario)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", ((Number) fila[0]).intValue());
            item.put("titulo", (String) fila[1]);
            item.put("materia", (String) fila[3]);
            item.put("fecha", aLocalDate(fila[6]).format(fechaFmt));
            item.put("hora", aLocalTime(fila[7]).format(horaFmt));
            item.put("inscritos", ((Number) fila[14]).intValue());
            item.put("cupoMax", ((Number) fila[9]).intValue());
            item.put("enlace", "");
            lista.add(item);
        }
        return lista;
    }

    private LocalDate aLocalDate(Object valor) {
        if (valor instanceof LocalDate ld) return ld;
        if (valor instanceof java.sql.Date sd) return sd.toLocalDate();
        if (valor instanceof java.sql.Timestamp ts) return ts.toLocalDateTime().toLocalDate();
        if (valor instanceof LocalDateTime ldt) return ldt.toLocalDate();
        if (valor instanceof java.util.Date d) return d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        throw new IllegalArgumentException("Tipo de fecha inesperado: " + (valor == null ? "null" : valor.getClass().getName()));
    }

    private LocalTime aLocalTime(Object valor) {
        if (valor instanceof LocalTime lt) return lt;
        if (valor instanceof java.sql.Time st) return st.toLocalTime();
        if (valor instanceof java.sql.Timestamp ts) return ts.toLocalDateTime().toLocalTime();
        if (valor instanceof java.util.Date d) return d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalTime();
        throw new IllegalArgumentException("Tipo de hora inesperado: " + (valor == null ? "null" : valor.getClass().getName()));
    }
}


