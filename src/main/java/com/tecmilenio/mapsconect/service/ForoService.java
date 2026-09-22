package com.tecmilenio.mapsconect.service;

import com.tecmilenio.mapsconect.dto.CrearPublicacionDTO;
import com.tecmilenio.mapsconect.dto.CrearRespuestaDTO;
import com.tecmilenio.mapsconect.dto.DuplicadoDTO;
import com.tecmilenio.mapsconect.dto.PublicacionDTO;
import com.tecmilenio.mapsconect.dto.RecursoDTO;
import com.tecmilenio.mapsconect.dto.RespuestaForoDTO;
import com.tecmilenio.mapsconect.dto.TipDTO;
import com.tecmilenio.mapsconect.entity.Estudiante;
import com.tecmilenio.mapsconect.entity.Materia;
import com.tecmilenio.mapsconect.entity.Publicacion;
import com.tecmilenio.mapsconect.entity.Respuesta;
import com.tecmilenio.mapsconect.entity.Usuario;
import com.tecmilenio.mapsconect.exception.ResourceNotFoundException;
import com.tecmilenio.mapsconect.repository.EstudianteRepository;
import com.tecmilenio.mapsconect.repository.MateriaRepository;
import com.tecmilenio.mapsconect.repository.PublicacionRepository;
import com.tecmilenio.mapsconect.repository.RecursoAcademicoRepository;
import com.tecmilenio.mapsconect.repository.RespuestaRepository;
import com.tecmilenio.mapsconect.repository.SeguimientoRepository;
import com.tecmilenio.mapsconect.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio del foro académico.
 *
 * <p>Gestiona la publicación de dudas por estudiantes y la detección
 * automática de preguntas duplicadas mediante una métrica de similitud
 * de tipo Dice coefficient sobre tokens lematizados (sin acentos, sin
 * stopwords). También arma el contexto completo de un duplicado:
 * respuestas, tips y recursos relacionados.</p>
 *
 * <p>Notifica a los profesores cuando se publica una nueva duda.</p>
 *
 * @see ForoService
 */
@Service
public class ForoService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("d 'de' MMMM");

    private static final Set<String> STOPWORDS = Set.of(
            "de", "la", "el", "en", "y", "a", "los", "las", "un", "una", "que", "es", "con",
            "por", "para", "se", "del", "su", "al", "como", "mas", "más", "pero", "sus", "me",
            "mi", "tu", "si", "ya", "lo", "le", "o", "u", "ni", "este", "esta", "esto");

    @Autowired
    private PublicacionRepository publicacionRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private MateriaRepository materiaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SeguimientoRepository seguimientoRepository;

    @Autowired
    private RecursoAcademicoRepository recursoAcademicoRepository;

    @Autowired
    private RespuestaRepository respuestaRepository;

    @Autowired
    private TipService tipService;

    @Autowired
    private CarreraContextoService carreraContextoService;

    @Autowired
    private NotificacionService notificacionService;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Busca una pregunta similar ya existente. Devuelve vacío si no hay duplicado
     * o si el autor pidió explícitamente ignorar la revisión.
     */
    public Optional<DuplicadoDTO> buscarDuplicado(String email, CrearPublicacionDTO request) {
        if (request.isIgnorarDuplicado()) {
            return Optional.empty();
        }

        String titulo = request.getTitulo() == null ? "" : request.getTitulo();
        String contenido = request.getContenido() == null ? "" : request.getContenido();

        Set<String> tituloNuevo = tokens(titulo);
        Set<String> todoNuevo = tokens(titulo + " " + contenido);

        Usuario yo = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Publicacion mejor = null;
        double mejorSimilitud = 0.0;

        for (Publicacion existente : publicacionRepository.findAll()) {
            Set<String> tituloAl = tokens(existente.getTitulo());
            Set<String> todoAl = tokens(existente.getTitulo() + " " + (existente.getContenido() == null ? "" : existente.getContenido()));

            double similitudTitulo = dice(tituloNuevo, tituloAl);
            double similitudTodo = dice(todoNuevo, todoAl);

            // Regla heurística: se considera duplicado si el título coincide bastante,
            // o si el texto completo se solapa lo suficiente, o si ambos se parecen
            // de forma moderada a la vez.
            boolean esDuplicado = similitudTitulo >= 0.55
                    || similitudTodo >= 0.45
                    || (similitudTitulo >= 0.30 && similitudTodo >= 0.30);

            double similitud = Math.max(similitudTitulo, similitudTodo);

            if (esDuplicado && similitud > mejorSimilitud) {
                mejorSimilitud = similitud;
                mejor = existente;
            }
        }

        if (mejor == null) {
            return Optional.empty();
        }

        return Optional.of(armarDuplicado(mejor, mejorSimilitud, yo.getId()));
    }

    @Transactional
    public PublicacionDTO publicar(String email, CrearPublicacionDTO request) {
        Usuario autor = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (autor.getRol() != Usuario.Rol.ESTUDIANTE) {
            throw new IllegalArgumentException("Solo los estudiantes pueden publicar dudas");
        }

        Estudiante estudiante = estudianteRepository.findByIdUsuario(autor.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tu perfil de estudiante no está completo. Completa tu onboarding para publicar dudas."));

        if (request.getIdMateria() != null && !existeMateria(request.getIdMateria())) {
            throw new IllegalArgumentException("La materia indicada no existe");
        }

        if (carreraContextoService.esEstudianteConCarrera(email)
                && request.getIdMateria() != null
                && !carreraContextoService.materiaEsDeCarrera(email, request.getIdMateria())) {
            throw new IllegalArgumentException("La materia no pertenece al plan de tu carrera");
        }

        Publicacion publicacion = Publicacion.builder()
                .idEstudiante(estudiante.getId())
                .idCarrera(estudiante.getIdCarrera())
                .idMateria(request.getIdMateria())
                .titulo(request.getTitulo().trim())
                .contenido(request.getContenido().trim())
                .fechaPublicacion(LocalDateTime.now())
                .estado("abierta")
                .build();

        publicacion = publicacionRepository.save(publicacion);

        notificarDocentesNuevaDuda(publicacion, autor);

        return toPublicacionDTO(publicacion, autor, estudiante, publicacionRepository
                .findRespuestasConAutor(publicacion.getId()).size());
    }

    public List<RespuestaForoDTO> listarRespuestas(Integer idPublicacion) {
        if (!publicacionRepository.existsById(idPublicacion)) {
            throw new ResourceNotFoundException("Publicación no encontrada");
        }
        return publicacionRepository.findRespuestasConAutor(idPublicacion).stream()
                .map(this::mapearRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional
    public RespuestaForoDTO responder(Integer idPublicacion, String email, CrearRespuestaDTO request) {
        Publicacion publicacion = publicacionRepository.findById(idPublicacion)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada"));

        Usuario autor = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        boolean esDocente = autor.getRol() == Usuario.Rol.PROFESOR;

        Respuesta respuesta = Respuesta.builder()
                .idPublicacion(idPublicacion)
                .idUsuario(autor.getId())
                .contenido(request.getContenido().trim())
                .fechaRespuesta(LocalDateTime.now())
                .esVerificadaDocente(esDocente)
                .esSolucion(false)
                .build();

        respuesta = respuestaRepository.save(respuesta);

        // Notificar al autor de la publicación si es un usuario distinto
        estudianteRepository.findById(publicacion.getIdEstudiante()).ifPresent(est -> {
            if (!est.getIdUsuario().equals(autor.getId())) {
                String preview = (request.getContenido().length() > 80)
                        ? request.getContenido().substring(0, 77) + "..."
                        : request.getContenido();
                notificacionService.notificar(
                        est.getIdUsuario(),
                        "foro",
                        "Nueva respuesta de " + autor.getNombreCompleto(),
                        preview,
                        "comunidad.html",
                        publicacion.getId()
                );
            }
        });

        return RespuestaForoDTO.builder()
                .id(respuesta.getId())
                .idPublicacion(publicacion.getId())
                .contenido(respuesta.getContenido())
                .autor(autor.getNombreCompleto())
                .autorId(autor.getId())
                .autorFoto(autor.getFotoUrl())
                .fecha("Recientemente")
                .esSolucion(false)
                .esVerificadaDocente(esDocente)
                .build();
    }

    public RespuestaForoDTO mapearRespuesta(Object[] fila) {
        return RespuestaForoDTO.builder()
                .id(((Number) fila[0]).intValue())
                .idPublicacion(((Number) fila[1]).intValue())
                .contenido((String) fila[2])
                .esSolucion(esVerdadero(fila[3]))
                .fecha(aLocalDateTime(fila[4]) != null ? aLocalDateTime(fila[4]).format(FECHA) : "")
                .autorId(fila[5] == null ? null : ((Number) fila[5]).intValue())
                .autor((String) fila[6])
                .autorFoto(fila.length > 7 && fila[7] != null ? String.valueOf(fila[7]) : null)
                .esVerificadaDocente(fila.length > 8 && esVerdadero(fila[8]))
                .build();
    }

    private void notificarDocentesNuevaDuda(Publicacion publicacion, Usuario autor) {
        String materia = publicacion.getIdMateria() != null
                ? materiaRepository.findById(publicacion.getIdMateria()).map(Materia::getNombre).orElse(null)
                : null;
        String titulo = "Nueva duda de " + autor.getNombreCompleto();
        String preview = (materia != null ? "[" + materia + "] " : "")
                + (publicacion.getTitulo() != null ? publicacion.getTitulo() : "Sin título");
        String enlace = "comunidad.html";
        for (Usuario docente : usuarioRepository.findByRolAndActivoTrue(Usuario.Rol.PROFESOR)) {
            notificacionService.notificar(docente.getId(), "duda", titulo, preview, enlace,
                    publicacion.getId());
        }
    }

    // ---- Armado del DTO de duplicado con todo el contexto (respuestas, tips, recursos) ----

    private DuplicadoDTO armarDuplicado(Publicacion existente, double similitud, Integer idYo) {
        List<Object[]> detalle = publicacionRepository.findForoDetallePorId(existente.getId());

        PublicacionDTO publicacion;
        if (detalle.isEmpty()) {
            publicacion = PublicacionDTO.builder()
                    .id(existente.getId())
                    .titulo(existente.getTitulo())
                    .descripcion(existente.getContenido())
                    .idMateria(existente.getIdMateria())
                    .respuestas(0)
                    .build();
        } else {
            publicacion = mapearDetalle(detalle.get(0), idYo);
        }

        List<RespuestaForoDTO> respuestas = publicacionRepository
                .findRespuestasConAutor(existente.getId()).stream()
                .map(this::mapearRespuesta)
                .collect(Collectors.toList());

        List<TipDTO> tips = tipService.listarPorMateria(existente.getIdMateria());

        List<RecursoDTO> recursos = (existente.getIdMateria() == null)
                ? List.of()
                : recursoAcademicoRepository.findRecursosVisiblesPorMateria(existente.getIdMateria()).stream()
                        .map(this::mapearRecurso)
                        .collect(Collectors.toList());

        return DuplicadoDTO.builder()
                .publicacion(publicacion)
                .similitud(similitud)
                .respuestas(respuestas)
                .tips(tips)
                .recursos(recursos)
                .build();
    }

    private PublicacionDTO mapearDetalle(Object[] fila, Integer idYo) {
        Integer id = ((Number) fila[0]).intValue();
        String titulo = (String) fila[1];
        String contenido = (String) fila[2];
        Integer idMateria = fila[4] == null ? null : ((Number) fila[4]).intValue();
        String autor = (String) fila[5];
        String materia = (String) fila[6];
        boolean resuelto = esVerdadero(fila[7]);
        String solucion = (String) fila[8];
        Integer autorId = fila[9] == null ? null : ((Number) fila[9]).intValue();
        String semestre = fila[10] == null ? null : String.valueOf(((Number) fila[10]).intValue());
        String autorFoto = fila[11] == null ? null : String.valueOf(fila[11]);
        int respuestas = publicacionRepository.findRespuestasConAutor(id).size();

        boolean siguiendo = autorId != null
                && seguimientoRepository.existsByIdSeguidorAndIdSeguido(idYo, autorId);

        return PublicacionDTO.builder()
                .id(id)
                .titulo(titulo)
                .descripcion(contenido)
                .idMateria(idMateria)
                .materia(materia)
                .autor(autor)
                .tiempo(aLocalDateTime(fila[3]) != null ? aLocalDateTime(fila[3]).format(FECHA) : "")
                .votos(0)
                .respuestas(respuestas)
                .resuelto(resuelto)
                .solucion(solucion)
                .autorId(autorId)
                .autorFoto(autorFoto)
                .siguiendo(siguiendo)
                .semestre(semestre)
                .build();
    }

    private PublicacionDTO toPublicacionDTO(Publicacion p, Usuario autor, Estudiante estudiante, int numRespuestas) {
        return PublicacionDTO.builder()
                .id(p.getId())
                .titulo(p.getTitulo())
                .descripcion(p.getContenido())
                .idMateria(p.getIdMateria())
                .materia(p.getIdMateria() == null ? null : nombreMateria(p.getIdMateria()))
                .autor(autor.getNombreCompleto())
                .tiempo(p.getFechaPublicacion().format(FECHA))
                .votos(0)
                .respuestas(numRespuestas)
                .resuelto(false)
                .solucion(null)
                .autorId(autor.getId())
                .autorFoto(autor.getFotoUrl())
                .siguiendo(false)
                .semestre(String.valueOf(estudiante.getSemestreActual()))
                .build();
    }

    private RecursoDTO mapearRecurso(Object[] fila) {
        String url = (String) fila[3];
        String adjuntoNombre = fila.length > 9 && fila[9] != null ? (String) fila[9] : null;
        Long adjuntoTamano = fila.length > 10 && fila[10] != null ? ((Number) fila[10]).longValue() : null;
        return RecursoDTO.builder()
                .id(((Number) fila[0]).intValue())
                .titulo((String) fila[1])
                .descripcion((String) fila[2])
                .url(url)
                .tipo(fila[4] == null ? "PDF" : (String) fila[4])
                .descargas(fila[6] == null ? 0 : ((Number) fila[6]).intValue())
                .autor((String) fila[7])
                .materia((String) fila[8])
                .tiempo("")
                .interno(url != null && url.startsWith("archivo:"))
                .adjuntoNombre(adjuntoNombre)
                .adjuntoTamano(adjuntoTamano)
                .build();
    }

    // ---- Normalización y similitud ----

    private Set<String> tokens(String texto) {
        if (texto == null || texto.isBlank()) {
            return new HashSet<>();
        }
        String normalizado = Normalizer.normalize(texto.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        Set<String> resultado = new HashSet<>();
        for (String palabra : normalizado.split(" ")) {
            if (palabra.length() >= 3 && !STOPWORDS.contains(palabra)) {
                resultado.add(lematizar(palabra));
            }
        }
        return resultado;
    }

    /**
     * Lemmatización ligera para español: reduce plurales ("ejemplos"→"ejemplo",
     * "bases"→"base") para que dos redacciones de la misma idea coincidan mejor.
     */
    private static String lematizar(String palabra) {
        if (palabra.length() > 4) {
            if (palabra.endsWith("es") && !palabra.endsWith("iones")) {
                return palabra.substring(0, palabra.length() - 2);
            }
            if (palabra.endsWith("s")) {
                return palabra.substring(0, palabra.length() - 1);
            }
        }
        return palabra;
    }

    private double dice(Set<String> a, Set<String> b) {
        if (a.isEmpty() && b.isEmpty()) {
            return 1.0;
        }
        if (a.isEmpty() || b.isEmpty()) {
            return 0.0;
        }
        Set<String> interseccion = new HashSet<>(a);
        interseccion.retainAll(b);
        return (2.0 * interseccion.size()) / (a.size() + b.size());
    }

    // ---- Helpers ----

    private boolean existeMateria(Integer idMateria) {
        Number conteo = (Number) entityManager
                .createNativeQuery("SELECT COUNT(*) FROM materias WHERE id_materia = ?1")
                .setParameter(1, idMateria)
                .getSingleResult();
        return conteo.intValue() > 0;
    }

    private String nombreMateria(Integer idMateria) {
        if (idMateria == null) {
            return null;
        }
        List<Object[]> filas = entityManager
                .createNativeQuery("SELECT id_materia, nombre_materia FROM materias WHERE id_materia = ?1")
                .setParameter(1, idMateria)
                .getResultList();
        if (filas.isEmpty()) {
            return null;
        }
        return (String) filas.get(0)[1];
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

