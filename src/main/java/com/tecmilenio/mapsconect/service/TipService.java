package com.tecmilenio.mapsconect.service;

import com.tecmilenio.mapsconect.dto.TipDTO;
import com.tecmilenio.mapsconect.entity.TipAcademico;
import com.tecmilenio.mapsconect.entity.Usuario;
import com.tecmilenio.mapsconect.entity.VotoTip;
import com.tecmilenio.mapsconect.exception.ResourceNotFoundException;
import com.tecmilenio.mapsconect.repository.TipRepository;
import com.tecmilenio.mapsconect.repository.UsuarioRepository;
import com.tecmilenio.mapsconect.repository.VotoTipRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio de tips académicos.
 *
 * <p>Los tips son publicaciones breves de contenido académico (consejos,
 * trucos, recursos útiles) asociadas a una materia específica. Los
 * estudiantes pueden crear tips, votarlos (sistema de upvotes sin downvote)
 * y retirar su voto.</p>
 *
 * <p>El listado se filtra por la carrera del estudiante (solo ve tips de
 * sus materias); profesores y administradores ven todos los tips.</p>
 *
 * <p>Los tokens de materia se resuelven mediante consultas nativas al
 * {@link EntityManager}.</p>
 */
@Service
public class TipService {

    private static final DateTimeFormatter FECHA_TIP = DateTimeFormatter.ofPattern("d 'de' MMMM");

    @Autowired
    private TipRepository tipRepository;

    @Autowired
    private VotoTipRepository votoTipRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CarreraContextoService carreraContextoService;

    @PersistenceContext
    private EntityManager entityManager;

    public List<TipDTO> listar() {
        return tipRepository.findAllByOrderByTotalVotosDesc().stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    /**
     * Lista los tips visibles para el usuario. Los estudiantes solo ven los
     * tips de las materias de su carrera; profesores/administradores ven todos.
     */
    public List<TipDTO> listarPara(String email) {
        Set<Integer> materiasCarrera = carreraContextoService.materiasDeCarrera(email);
        return tipRepository.findAllByOrderByTotalVotosDesc().stream()
                .filter(t -> materiasCarrera == null || materiasCarrera.contains(t.getIdMateria()))
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    public List<TipDTO> listarPorMateria(Integer idMateria) {
        if (idMateria == null) {
            return List.of();
        }
        return tipRepository.findByIdMateriaOrderByTotalVotosDesc(idMateria).stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TipDTO crear(String email, String contenido, Integer idMateria) {
        Usuario autor = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (idMateria == null || !existeMateria(idMateria)) {
            throw new IllegalArgumentException("Debes indicar una materia válida");
        }

        if (carreraContextoService.esEstudianteConCarrera(email)
                && !carreraContextoService.materiaEsDeCarrera(email, idMateria)) {
            throw new IllegalArgumentException("La materia no pertenece al plan de tu carrera");
        }

        TipAcademico tip = TipAcademico.builder()
                .autor(autor)
                .idMateria(idMateria)
                .contenido(contenido)
                .fechaPublicacion(LocalDateTime.now())
                .totalVotos(0)
                .build();

        return mapearADTO(tipRepository.save(tip));
    }

    public TipDTO votar(String email, Integer idTip) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        TipAcademico tip = tipRepository.findById(idTip)
                .orElseThrow(() -> new ResourceNotFoundException("Tip no encontrado"));

        if (votoTipRepository.existsByIdTipAndIdUsuario(idTip, usuario.getId())) {
            throw new IllegalArgumentException("Ya has votado este tip");
        }

        votoTipRepository.save(VotoTip.builder()
                .idTip(idTip)
                .idUsuario(usuario.getId())
                .fechaVoto(LocalDateTime.now())
                .build());

        return mapearADTO(tipRepository.findById(idTip).orElseThrow());
    }

    @Transactional
    public void desvotar(String email, Integer idTip) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        VotoTip voto = votoTipRepository.findByIdTipAndIdUsuario(idTip, usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException("No has votado este tip"));

        votoTipRepository.delete(voto);
    }

    private TipDTO mapearADTO(TipAcademico tip) {
        String nombreMateria = obtenerNombreMateria(tip.getIdMateria());

        String titulo = tip.getContenido() == null ? "" : tip.getContenido().trim();
        if (titulo.contains("\n")) {
            titulo = titulo.split("\n", 2)[0];
        }
        if (titulo.length() > 60) {
            titulo = titulo.substring(0, 60) + "…";
        }

        return TipDTO.builder()
                .id(tip.getId())
                .titulo(titulo)
                .texto(tip.getContenido())
                .idMateria(tip.getIdMateria())
                .materia(nombreMateria)
                .autorId(tip.getAutor().getId())
                .autor(tip.getAutor().getNombreCompleto())
                .verificado(tip.getAutor().getRol() == Usuario.Rol.PROFESOR)
                .votos(tip.getTotalVotos() == null ? 0 : tip.getTotalVotos())
                .fechaPublicacion(tip.getFechaPublicacion().format(FECHA_TIP))
                .build();
    }

    private String obtenerNombreMateria(Integer idMateria) {
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

    private boolean existeMateria(Integer idMateria) {
        Number conteo = (Number) entityManager
                .createNativeQuery("SELECT COUNT(*) FROM materias WHERE id_materia = ?1")
                .setParameter(1, idMateria)
                .getSingleResult();
        return conteo.intValue() > 0;
    }

}

