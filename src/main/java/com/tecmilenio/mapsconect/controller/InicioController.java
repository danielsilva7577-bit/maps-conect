package com.tecmilenio.mapsconect.controller;

import com.tecmilenio.mapsconect.exception.ResourceNotFoundException;

import com.tecmilenio.mapsconect.dto.ApiResponse;
import com.tecmilenio.mapsconect.dto.MateriaSimpleDTO;
import com.tecmilenio.mapsconect.entity.Certificado;
import com.tecmilenio.mapsconect.entity.Estudiante;
import com.tecmilenio.mapsconect.entity.EstudianteCertificado;
import com.tecmilenio.mapsconect.entity.EstudianteMateria;
import com.tecmilenio.mapsconect.entity.PlanEstudios;
import com.tecmilenio.mapsconect.entity.SesionRepaso;
import com.tecmilenio.mapsconect.entity.Usuario;
import com.tecmilenio.mapsconect.repository.CarreraRepository;
import com.tecmilenio.mapsconect.repository.CertificadoRepository;
import com.tecmilenio.mapsconect.repository.EmpresaVinculadaRepository;
import com.tecmilenio.mapsconect.repository.EstudianteCertificadoRepository;
import com.tecmilenio.mapsconect.repository.EstudianteMateriaRepository;
import com.tecmilenio.mapsconect.repository.EstudianteRepository;
import com.tecmilenio.mapsconect.repository.MateriaRepository;
import com.tecmilenio.mapsconect.repository.PlanEstudiosRepository;
import com.tecmilenio.mapsconect.repository.PublicacionRepository;
import com.tecmilenio.mapsconect.repository.SeguimientoRepository;
import com.tecmilenio.mapsconect.repository.SesionRepasoRepository;
import com.tecmilenio.mapsconect.repository.UsuarioRepository;
import com.tecmilenio.mapsconect.service.CarreraContextoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Controlador del dashboard de inicio (feed personalizado).
 *
 * <p>Agrega una única instancia de {@link java.time.DateTimeFormatter}
 * y varios repositorios para construir el feed principal que ve el usuario
 * al entrar a la plataforma.</p>
 *
 * <p>El dashboard incluye:</p>
 * <ul>
 *   <li>Datos del perfil del usuario (nombre, foto, reputación, etc.)</li>
 *   <li>Materias inscritas del semestre actual</li>
 *   <li>Publicaciones recientes del foro (dudas y respuestas)</li>
 *   <li>Próxima sesión de repaso disponible</li>
 *   <li>Empresa destacada según la carrera del usuario</li>
 * </ul>
 *
 * <p>No requiere autenticación explícita: si el usuario no está autenticado
 * se construye un feed parcial.</p>
 */
@RestController
@RequestMapping("/inicio")
public class InicioController {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("d 'de' MMMM");
    private static final DateTimeFormatter FECHA_SESION = DateTimeFormatter
            .ofPattern("EEE d 'de' MMMM, HH:mm", Locale.forLanguageTag("es-MX"));

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private EstudianteCertificadoRepository estudianteCertificadoRepository;

    @Autowired
    private CarreraRepository carreraRepository;

    @Autowired
    private EstudianteMateriaRepository estudianteMateriaRepository;

    @Autowired
    private MateriaRepository materiaRepository;

    @Autowired
    private CertificadoRepository certificadoRepository;

    @Autowired
    private PlanEstudiosRepository planEstudiosRepository;

    @Autowired
    private PublicacionRepository publicacionRepository;

    @Autowired
    private EmpresaVinculadaRepository empresaRepository;

    @Autowired
    private SeguimientoRepository seguimientoRepository;

    @Autowired
    private SesionRepasoRepository sesionRepasoRepository;

    @Autowired
    private CarreraContextoService carreraContextoService;

    /**
     * Construye el dashboard personalizado del usuario autenticado.
     *
     * <p>Incluye perfil, materias, publicaciones recientes, próxima sesión
     * de repaso y empresa destacada según la carrera del usuario.</p>
     *
     * @param authentication usuario autenticado
     * @return 200 OK con todos los datos del dashboard
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> dashboard(Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        String carrera = carreraContextoService.carreraDelUsuario(usuario.getEmail());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("perfil", perfil(usuario));
        data.put("materias", materias(usuario));
        data.put("publicaciones", publicaciones(usuario));
        data.put("circuloActual", proximaSesion());
        data.put("asesoria", null);
        data.put("empresaDestacada", empresaDestacada(usuario));
        if (carrera != null) {
            data.put("carreraFiltrada", carrera);
        }

        return ResponseEntity.ok(ApiResponse.success(data, "Dashboard obtenido"));
    }

    private Map<String, Object> perfil(Usuario usuario) {
        Map<String, Object> perfil = new LinkedHashMap<>();
        Estudiante estudiante = estudianteRepository.findByIdUsuario(usuario.getId()).orElse(null);

        String carrera = null;
        Integer semestre = null;
        if (estudiante != null) {
            semestre = estudiante.getSemestreActual();
            if (estudiante.getIdCarrera() != null) {
                carrera = carreraRepository.findById(estudiante.getIdCarrera())
                        .map(c -> c.getNombre()).orElse(null);
            }
        }

        perfil.put("nombre", usuario.getNombreCompleto());
        perfil.put("email", usuario.getEmail());
        perfil.put("rol", usuario.getRol().name());
        perfil.put("foto", usuario.getFotoUrl());
        perfil.put("seguidores", seguimientoRepository.countByIdSeguido(usuario.getId()));
        perfil.put("siguiendo", seguimientoRepository.countByIdSeguidor(usuario.getId()));
        perfil.put("carrera", carrera);
        perfil.put("certificado", certificadoActivo(estudiante));
        perfil.put("semestre", semestre);
        perfil.put("totalSemestres", totalSemestres(estudiante));
        return perfil;
    }

    private String certificadoActivo(Estudiante estudiante) {
        if (estudiante == null) {
            return null;
        }

        String certificado = null;
        for (EstudianteCertificado asignacion : estudianteCertificadoRepository
                .findByIdEstudianteOrderByFechaSeleccionAsc(estudiante.getId())) {
            certificado = certificadoRepository.findById(asignacion.getIdCertificado())
                    .map(Certificado::getNombre)
                    .orElse(certificado);
        }
        return certificado;
    }

    private Integer totalSemestres(Estudiante estudiante) {
        if (estudiante == null || estudiante.getIdCarrera() == null) {
            return null;
        }
        return planEstudiosRepository.findByCarreraId(estudiante.getIdCarrera()).stream()
                .map(PlanEstudios::getSemestre)
                .filter(semestre -> semestre != null)
                .max(Integer::compareTo)
                .orElse(null);
    }

    private List<MateriaSimpleDTO> materias(Usuario usuario) {
        List<MateriaSimpleDTO> resultado = new ArrayList<>();
        Estudiante estudiante = estudianteRepository.findByIdUsuario(usuario.getId()).orElse(null);
        if (estudiante == null) {
            return resultado;
        }

        List<EstudianteMateria> asignaciones = estudianteMateriaRepository.findByIdEstudiante(estudiante.getId());
        for (EstudianteMateria asig : asignaciones) {
            materiaRepository.findById(asig.getIdMateria()).ifPresent(m -> resultado.add(
                    MateriaSimpleDTO.builder().id(m.getId()).nombre(m.getNombre()).build()));
        }
        return resultado;
    }

    private List<Map<String, Object>> publicaciones(Usuario usuario) {
        List<Map<String, Object>> lista = new ArrayList<>();
        Set<Integer> materiasCarrera = carreraContextoService.materiasDeCarrera(usuario.getEmail());

        Map<Integer, Object[]> respuestasUsuario = new HashMap<>();
        for (Object[] r : publicacionRepository.findRespuestasDeUsuario(usuario.getId())) {
            respuestasUsuario.putIfAbsent(((Number) r[0]).intValue(), r);
        }

        for (Object[] fila : publicacionRepository.findForoConDetalles(10)) {
            Object idMateriaObj = fila[5];
            if (materiasCarrera != null && idMateriaObj != null
                    && !materiasCarrera.contains(((Number) idMateriaObj).intValue())) {
                continue;
            }
            int id = ((Number) fila[0]).intValue();
            Integer autorId = fila[12] == null ? null : ((Number) fila[12]).intValue();
            Map<String, Object> autor = new LinkedHashMap<>();
            autor.put("id", autorId);
            autor.put("nombre", fila[6]);
            autor.put("foto", fila[13] == null ? null : String.valueOf(fila[13]));
            autor.put("seguido", autorId != null
                    && seguimientoRepository.existsByIdSeguidorAndIdSeguido(usuario.getId(), autorId));
            autor.put("tipo", "estudiante");
            autor.put("semestre", fila[11] == null ? null : ((Number) fila[11]).intValue());

            int respuestas = fila[8] == null ? 0 : ((Number) fila[8]).intValue();
            Map<String, Object> pub = new LinkedHashMap<>();
            pub.put("id", id);
            pub.put("titulo", fila[1]);
            pub.put("contenido", fila[2]);
            pub.put("materia", fila[7]);
            pub.put("autor", autor);
            pub.put("respuestas", respuestas);
            pub.put("solucionAceptada", esVerdadero(fila[9]));
            // Participación del usuario: respondió y su respuesta fue aceptada en este hilo.
            Object[] miRespuesta = respuestasUsuario.get(id);
            pub.put("participado", miRespuesta != null);
            pub.put("respuestaUsuario", miRespuesta == null ? null : miRespuesta[1]);
            Object fechaObj = fila[4];
            pub.put("fechaRelativa", fechaObj instanceof LocalDateTime lt ? relativa(lt) : fechaObj instanceof java.sql.Timestamp ts ? relativa(ts.toLocalDateTime()) : "");
            pub.put("tipoContenido", "duda");
            lista.add(pub);
        }
        return lista;
    }

    private static String relativa(LocalDateTime lt) {
        long min = Duration.between(lt, LocalDateTime.now()).toMinutes();
        if (min < 1) return "hace un momento";
        if (min < 60) return "hace " + min + " min";
        long horas = min / 60;
        if (horas < 24) return horas == 1 ? "hace 1 hora" : "hace " + horas + " horas";
        long dias = horas / 24;
        if (dias == 1) return "ayer";
        if (dias <= 7) return "hace " + dias + " días";
        return lt.format(FECHA);
    }

    private static boolean esVerdadero(Object o) {
        if (o instanceof Boolean b) return b;
        if (o instanceof Number n) return n.intValue() == 1;
        return false;
    }

    private Map<String, Object> proximaSesion() {
        return sesionRepasoRepository.findAll().stream()
                .filter(sesion -> sesion.getFecha() != null
                        && !sesion.getFecha().isBefore(LocalDate.now()))
                .filter(sesion -> sesion.getEstado() == null
                        || !"CERRADA".equalsIgnoreCase(sesion.getEstado()))
                .min(Comparator.comparing(SesionRepaso::getFecha)
                        .thenComparing(SesionRepaso::getHoraInicio))
                .map(sesion -> {
                    Map<String, Object> circulo = new LinkedHashMap<>();
                    circulo.put("fecha", sesion.getFecha().format(FECHA_SESION));
                    circulo.put("nombre", sesion.getTitulo());
                    circulo.put("descripcion", sesion.getDescripcion());
                    return circulo;
                })
                .orElse(null);
    }

    private Map<String, Object> empresaDestacada(Usuario usuario) {
        Set<String> tokensCarrera = carreraContextoService.tokensSignificativos(
                carreraContextoService.carreraDelUsuario(usuario.getEmail()));

        List<Object[]> filas = empresaRepository.findEmpresasConPromedio();
        Object[] mejor = null;
        for (Object[] fila : filas) {
            if (fila[6] == null) {
                continue;
            }
            double cal = ((Number) fila[6]).doubleValue();
            if (mejor == null || cal > ((Number) mejor[6]).doubleValue()) {
                if (tokensCarrera == null || tokensCarrera.isEmpty()
                        || carreraContextoService.coincide((String) fila[5], tokensCarrera)) {
                    mejor = fila;
                }
            }
        }
        if (mejor == null) {
            return null;
        }

        Map<String, Object> empresa = new HashMap<>();
        empresa.put("nombre", mejor[1]);
        empresa.put("calificacion", mejor[6] == null ? null : ((Number) mejor[6]).doubleValue());
        empresa.put("resena", mejor[4] == null ? null : String.valueOf(mejor[4]));
        return empresa;
    }

}


