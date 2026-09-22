package com.tecmilenio.mapsconect.controller;

import com.tecmilenio.mapsconect.dto.ApiResponse;
import com.tecmilenio.mapsconect.dto.CrearSesionRepasoDTO;
import com.tecmilenio.mapsconect.dto.SesionRepasoDTO;
import com.tecmilenio.mapsconect.entity.SesionInscripcion;
import com.tecmilenio.mapsconect.entity.SesionRepaso;
import com.tecmilenio.mapsconect.entity.Usuario;
import com.tecmilenio.mapsconect.exception.ResourceNotFoundException;
import com.tecmilenio.mapsconect.repository.MiembroComunidadRepository;
import com.tecmilenio.mapsconect.repository.SesionInscripcionRepository;
import com.tecmilenio.mapsconect.repository.SesionRepasoRepository;
import com.tecmilenio.mapsconect.repository.UsuarioRepository;
import com.tecmilenio.mapsconect.service.CarreraContextoService;
import com.tecmilenio.mapsconect.service.NotificacionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Controlador de círculos de estudio y sesiones de repaso.
 *
 * <p>Gestiona la vista "Mis Círculos" donde los estudiantes pueden ver
 * las comunidades de estudio a las que pertenecen, las sesiones de repaso
 * programadas, inscribirse a ellas o darse de baja.</p>
 *
 * <p>También permite crear nuevas sesiones de repaso (disponibles para
 * estudiantes de la misma carrera o intercarreras).</p>
 *
 * @see com.tecmilenio.mapsconect.entity.SesionRepaso
 * @see com.tecmilenio.mapsconect.entity.SesionInscripcion
 */
@RestController
@RequestMapping("/circulos")
public class CirculosController {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("EEE d 'de' MMMM", Locale.forLanguageTag("es-MX"));

    @Autowired
    private MiembroComunidadRepository miembroRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SesionRepasoRepository sesionRepository;

    @Autowired
    private SesionInscripcionRepository inscripcionRepository;

    @Autowired
    private CarreraContextoService carreraContextoService;

    @Autowired
    private NotificacionService notificacionService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> listar(
            @RequestParam(required = false) String busqueda,
            Authentication authentication) {

        Usuario usuario = null;
        int idUsuario = 0;
        if (authentication != null && authentication.getName() != null) {
            usuario = usuarioRepository.findByEmail(authentication.getName()).orElse(null);
            if (usuario != null) {
                idUsuario = usuario.getId();
            }
        }

        List<Map<String, Object>> grupos = new ArrayList<>();
        if (usuario != null) {
            List<Object[]> filas = miembroRepository.findGruposDeUsuario(usuario.getId());
            for (Object[] fila : filas) {
                Map<String, Object> grupo = new LinkedHashMap<>();
                grupo.put("id", ((Number) fila[0]).intValue());
                grupo.put("nombre", (String) fila[1]);
                grupo.put("miembros", fila[2] == null ? 0 : ((Number) fila[2]).longValue());
                grupo.put("descripcion", "");
                if (busqueda == null || busqueda.isBlank()
                        || String.valueOf(fila[1]).toLowerCase().contains(busqueda.toLowerCase())) {
                    grupos.add(grupo);
                }
            }
        }

        final int idFinal = idUsuario;

        String email = usuario == null ? null : usuario.getEmail();
        java.util.Set<String> materiasPlan = carreraContextoService.nombresMateriasDeCarrera(email);

        List<SesionRepasoDTO> sesionesPendientes = sesionRepository.listarConInscripciones(idFinal).stream()
                .map(fila -> mapearSesion(fila, idFinal))
                .filter(s -> materiasPlan == null
                        || (s.getMateria() != null && carreraContextoService.materiaCoincideConPlan(s.getMateria(), materiasPlan)))
                .filter(s -> {
                    if (busqueda == null || busqueda.isBlank()) return true;
                    String q = busqueda.toLowerCase();
                    return (s.getTitulo() != null && s.getTitulo().toLowerCase().contains(q))
                            || (s.getMateria() != null && s.getMateria().toLowerCase().contains(q))
                            || (s.getOrganizador() != null && s.getOrganizador().toLowerCase().contains(q));
                })
                .collect(java.util.stream.Collectors.toList());

        List<SesionRepasoDTO> sesiones = sesionesPendientes.stream()
                .filter(s -> s.getFecha() != null && !LocalDate.parse(s.getFecha()).isBefore(LocalDate.now()))
                .collect(java.util.stream.Collectors.toList());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("sesiones", sesiones);
        data.put("misGrupos", grupos);
        return ResponseEntity.ok(ApiResponse.success(data, "Círculos obtenidos"));
    }

    @PostMapping("/sesiones")
    public ResponseEntity<ApiResponse<SesionRepasoDTO>> crearSesion(
            @Valid @RequestBody CrearSesionRepasoDTO request,
            Authentication authentication) {

        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        LocalDate fecha = parseFecha(request.getFecha());
        LocalTime hora = parseHora(request.getHoraInicio());

        if (fecha.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de la sesión no puede estar en el pasado");
        }

        String modalidad = request.getModalidad() == null || request.getModalidad().isBlank()
                ? "Grupal" : request.getModalidad().trim();

        SesionRepaso sesion = sesionRepository.save(SesionRepaso.builder()
                .titulo(request.getTitulo().trim())
                .descripcion(request.getDescripcion())
                .materia(request.getMateria().trim())
                .modalidad(modalidad)
                .ubicacion(request.getUbicacion())
                .fecha(fecha)
                .horaInicio(hora)
                .duracionMin(request.getDuracionMin() == null ? 90 : request.getDuracionMin())
                .cupoMax(request.getCupoMax() == null ? 30 : request.getCupoMax())
                .estado("ABIERTA")
                .organizadorId(usuario.getId())
                .creadoEn(LocalDateTime.now())
                .build());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(mapearDesdeEntidad(sesion, usuario), "Sesión de repaso creada"));
    }

    @PostMapping("/sesiones/{id}/inscribirse")
    public ResponseEntity<ApiResponse<SesionRepasoDTO>> inscribirse(
            @PathVariable Integer id, Authentication authentication) {

        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        SesionRepaso sesion = sesionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sesión de repaso no encontrada"));

        if (sesion.getOrganizadorId().equals(usuario.getId())) {
            throw new IllegalArgumentException("Eres el organizador de esta sesión");
        }
        if (sesion.getEstado().equalsIgnoreCase("CERRADA")) {
            throw new IllegalArgumentException("Esta sesión está cerrada");
        }

        long inscritos = inscripcionRepository.contar(id);
        if (inscritos >= sesion.getCupoMax()) {
            throw new IllegalArgumentException("La sesión ha alcanzado su cupo máximo");
        }
        if (inscripcionRepository.existe(id, usuario.getId())) {
            throw new IllegalArgumentException("Ya estás inscrito en esta sesión");
        }

        registrarInscripcion(id, usuario);

        notificarOrganizadorInscripcion(sesion, usuario);

        Object[] fila = sesionRepository.listarConInscripciones(usuario.getId()).stream()
                .filter(f -> ((Number) f[0]).intValue() == id)
                .findFirst().orElse(null);

        SesionRepasoDTO dto = fila == null
                ? mapearDesdeEntidad(sesion, usuario)
                : mapearSesion(fila, usuario.getId());

        return ResponseEntity.ok(ApiResponse.success(dto, "Asistencia confirmada"));
    }

    @DeleteMapping("/sesiones/{id}/inscribirse")
    public ResponseEntity<ApiResponse<SesionRepasoDTO>> salirse(
            @PathVariable Integer id, Authentication authentication) {

        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        SesionRepaso sesion = sesionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sesión de repaso no encontrada"));

        if (!inscripcionRepository.existe(id, usuario.getId())) {
            throw new IllegalArgumentException("No estás inscrito en esta sesión");
        }

        inscripcionRepository.eliminar(id, usuario.getId());

        Object[] fila = sesionRepository.listarConInscripciones(usuario.getId()).stream()
                .filter(f -> ((Number) f[0]).intValue() == id)
                .findFirst().orElse(null);

        SesionRepasoDTO dto = fila == null
                ? mapearDesdeEntidad(sesion, usuario)
                : mapearSesion(fila, usuario.getId());

        return ResponseEntity.ok(ApiResponse.success(dto, "Te has dado de baja de la sesión"));
    }

    private void registrarInscripcion(Integer id, Usuario usuario) {
        inscripcionRepository.save(SesionInscripcion.builder()
                .id(new SesionInscripcion.SesionInscripcionId(id, usuario.getId()))
                .fechaInscripcion(LocalDateTime.now())
                .build());
    }

    private void notificarOrganizadorInscripcion(SesionRepaso sesion, Usuario inscrito) {
        if (sesion.getOrganizadorId() == null
                || sesion.getOrganizadorId().equals(inscrito.getId())) {
            return;
        }
        String preview = "Se inscribió " + inscrito.getNombreCompleto()
                + (sesion.getTitulo() != null ? " a «" + sesion.getTitulo() + "»" : " a una sesión de repaso");
        notificacionService.notificar(
                sesion.getOrganizadorId(),
                "sesion",
                "Nuevo asistente a tu sesión",
                preview,
                "circulos.html",
                sesion.getId());
    }

    private SesionRepasoDTO mapearSesion(Object[] fila, int idUsuario) {
        Date fecha = (Date) fila[6];
        Time hora = (Time) fila[7];

        return SesionRepasoDTO.builder()
                .id(((Number) fila[0]).intValue())
                .titulo((String) fila[1])
                .descripcion((String) fila[2])
                .materia((String) fila[3])
                .modalidad((String) fila[4])
                .ubicacion((String) fila[5])
                .fecha(fecha.toLocalDate().toString())
                .horaInicio(hora.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                .duracionMin(((Number) fila[8]).intValue())
                .cupoMax(((Number) fila[9]).intValue())
                .estado((String) fila[10])
                .organizador((String) fila[11])
                .organizadorId(((Number) fila[12]).intValue())
                .inscritos(((Number) fila[14]).longValue())
                .inscrito(((Number) fila[15]).intValue() == 1)
                .organizadorYo(((Number) fila[12]).intValue() == idUsuario)
                .build();
    }

    private SesionRepasoDTO mapearDesdeEntidad(SesionRepaso sesion, Usuario organizador) {
        long inscritos = inscripcionRepository.contar(sesion.getId());
        return SesionRepasoDTO.builder()
                .id(sesion.getId())
                .titulo(sesion.getTitulo())
                .descripcion(sesion.getDescripcion())
                .materia(sesion.getMateria())
                .modalidad(sesion.getModalidad())
                .ubicacion(sesion.getUbicacion())
                .fecha(sesion.getFecha().toString())
                .horaInicio(sesion.getHoraInicio().format(DateTimeFormatter.ofPattern("HH:mm")))
                .duracionMin(sesion.getDuracionMin())
                .cupoMax(sesion.getCupoMax())
                .estado(sesion.getEstado())
                .organizador(organizador.getNombreCompleto())
                .organizadorId(sesion.getOrganizadorId())
                .inscritos(inscritos)
                .inscrito(false)
                .organizadorYo(true)
                .build();
    }

    private LocalDate parseFecha(String fecha) {
        try {
            return LocalDate.parse(fecha);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("La fecha debe tener formato AAAA-MM-DD");
        }
    }

    private LocalTime parseHora(String hora) {
        try {
            return LocalTime.parse(hora);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("La hora debe tener formato HH:MM");
        }
    }
}