package com.tecmilenio.mapsconect.controller;

import com.tecmilenio.mapsconect.exception.ResourceNotFoundException;

import com.tecmilenio.mapsconect.dto.ApiResponse;
import com.tecmilenio.mapsconect.dto.MateriaSimpleDTO;
import com.tecmilenio.mapsconect.dto.PropositoRequestDTO;
import com.tecmilenio.mapsconect.dto.SemestreRequestDTO;
import com.tecmilenio.mapsconect.entity.Carrera;
import com.tecmilenio.mapsconect.entity.Certificado;
import com.tecmilenio.mapsconect.entity.CertificadoMateria;
import com.tecmilenio.mapsconect.entity.Estudiante;
import com.tecmilenio.mapsconect.entity.EstudianteCertificado;
import com.tecmilenio.mapsconect.entity.EstudianteMateria;
import com.tecmilenio.mapsconect.entity.Materia;
import com.tecmilenio.mapsconect.entity.PlanEstudios;
import com.tecmilenio.mapsconect.entity.Usuario;
import com.tecmilenio.mapsconect.repository.CarreraRepository;
import com.tecmilenio.mapsconect.repository.CertificadoMateriaRepository;
import com.tecmilenio.mapsconect.repository.CertificadoRepository;
import com.tecmilenio.mapsconect.repository.EstudianteCertificadoRepository;
import com.tecmilenio.mapsconect.repository.EstudianteMateriaRepository;
import com.tecmilenio.mapsconect.repository.EstudianteRepository;
import com.tecmilenio.mapsconect.repository.MateriaRepository;
import com.tecmilenio.mapsconect.repository.PlanEstudiosRepository;
import com.tecmilenio.mapsconect.repository.SeguimientoRepository;
import com.tecmilenio.mapsconect.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controlador de la cuenta y ruta académica del estudiante.
 *
 * <p>Expone endpoints para consultar el perfil del estudiante, la ruta MAPS
 * (trayectoria de certificados y materias), y actualizar el propósito de
 * vida o cambiar de semestre.</p>
 *
 * <p>Al cambiar de semestre, se sincronizan las inscripciones de materias
 * del estudiante con las del plan de estudios correspondiente.</p>
 *
 * @see com.tecmilenio.mapsconect.entity.Estudiante
 * @see com.tecmilenio.mapsconect.entity.EstudianteMateria
 */
@RestController
@RequestMapping("/estudiantes")
public class EstudianteController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private CarreraRepository carreraRepository;

    @Autowired
    private CertificadoRepository certificadoRepository;

    @Autowired
    private EstudianteCertificadoRepository estudianteCertificadoRepository;

    @Autowired
    private SeguimientoRepository seguimientoRepository;

    @Autowired
    private MateriaRepository materiaRepository;

    @Autowired
    private EstudianteMateriaRepository estudianteMateriaRepository;

    @Autowired
    private PlanEstudiosRepository planEstudiosRepository;

    @Autowired
    private CertificadoMateriaRepository certificadoMateriaRepository;

    @GetMapping("/perfil")
    public ResponseEntity<ApiResponse<Map<String, Object>>> perfil(Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Map<String, Object> perfil = new LinkedHashMap<>();
        perfil.put("nombre", usuario.getNombreCompleto());
        perfil.put("email", usuario.getEmail());
        perfil.put("rol", usuario.getRol().name());
        perfil.put("foto", usuario.getFotoUrl());
        perfil.put("seguidores", seguimientoRepository.countByIdSeguido(usuario.getId()));
        perfil.put("siguiendo", seguimientoRepository.countByIdSeguidor(usuario.getId()));

        Estudiante estudiante = estudianteRepository.findByIdUsuario(usuario.getId()).orElse(null);
        if (estudiante != null) {
            perfil.put("matricula", estudiante.getMatricula());
            perfil.put("semestre", estudiante.getSemestreActual());
            perfil.put("proposito", estudiante.getPropositoVida());

            if (estudiante.getIdCarrera() != null) {
                carreraRepository.findById(estudiante.getIdCarrera())
                        .ifPresent(c -> perfil.put("carrera", c.getNombre()));
            }

            List<Map<String, Object>> certificados = new ArrayList<>();
            List<EstudianteCertificado> asignaciones = estudianteCertificadoRepository
                    .findByIdEstudianteOrderByFechaSeleccionAsc(estudiante.getId());
            for (EstudianteCertificado asig : asignaciones) {
                Optional<Certificado> cert = certificadoRepository.findById(asig.getIdCertificado());
                if (cert.isPresent()) {
                    Map<String, Object> c = new HashMap<>();
                    c.put("titulo", cert.get().getNombre());
                    c.put("descripcion", cert.get().getDescripcion());
                    c.put("estado", "encurso");
                    certificados.add(c);
                }
            }
            perfil.put("certificados", certificados);
        } else {
            perfil.put("matricula", "");
            perfil.put("semestre", null);
            perfil.put("proposito", null);
            perfil.put("carrera", null);
            perfil.put("certificados", List.of());
        }

        Map<String, Object> stats = new LinkedHashMap<>();
        // Valores demo para previsualizar la pantalla; reemplazar cuando se
        // computen las estadísticas reales del estudiante.
        stats.put("dudasResueltas", 2);
        stats.put("apuntesCompartidos", 4);
        stats.put("votos", 3);
        stats.put("reputacion", usuario.getPuntosReputacion() == null ? 0 : usuario.getPuntosReputacion());
        perfil.put("stats", stats);

        List<Map<String, Object>> actividad = new ArrayList<>();
        actividad.add(infoActividad("Compartiste un apunte", "Algoritmos avanzados: guía de repaso"));
        actividad.add(infoActividad("Tu respuesta fue marcada como solución",
                "Duda sobre normalización en bases de datos relacionales"));
        actividad.add(infoActividad("Votaste un tip útil", "Consejo para los exámenes de arquitectura"));
        actividad.add(infoActividad("Creaste un círculo de estudio", "Preparación Examen de Métodos Numéricos"));
        perfil.put("actividad", actividad);
        return ResponseEntity.ok(ApiResponse.success(perfil, "Perfil obtenido"));
    }

    private Map<String, Object> infoActividad(String titulo, String detalle) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("titulo", titulo);
        item.put("detalle", detalle);
        return item;
    }

    @GetMapping("/ruta")
    public ResponseEntity<ApiResponse<Map<String, Object>>> ruta(Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Estudiante estudiante = estudianteRepository.findByIdUsuario(usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Completa tu perfil de estudiante antes de consultar tu ruta MAPS"));

        Set<Integer> enRutaIds = estudianteCertificadoRepository
                .findByIdEstudianteOrderByFechaSeleccionAsc(estudiante.getId())
                .stream()
                .map(EstudianteCertificado::getIdCertificado)
                .collect(Collectors.toSet());

        List<Map<String, Object>> catalogo = new ArrayList<>();
        for (Certificado certificado : certificadoRepository.findAll()) {
            catalogo.add(detalleRuta(certificado, estudiante.getIdCarrera(), enRutaIds));
        }
        catalogo.sort(Comparator.comparing((Map<String, Object> c) ->
                c.get("semestreMin") == null ? Integer.MAX_VALUE : (Integer) c.get("semestreMin"))
                .thenComparing(c -> String.valueOf(c.get("nombre"))));

        List<Map<String, Object>> ruta = catalogo.stream()
                .filter(c -> Boolean.TRUE.equals(c.get("enRuta")))
                .collect(Collectors.toList());

        Map<String, Object> data = new LinkedHashMap<>();
        Map<String, Object> estudianteInfo = new LinkedHashMap<>();
        estudianteInfo.put("id", estudiante.getId());
        estudianteInfo.put("matricula", estudiante.getMatricula());
        estudianteInfo.put("semestre", estudiante.getSemestreActual());
        estudianteInfo.put("carrera", carreraRepository.findById(estudiante.getIdCarrera())
                .map(Carrera::getNombre).orElse(null));
        data.put("estudiante", estudianteInfo);
        data.put("ruta", ruta);
        data.put("catalogo", catalogo);
        return ResponseEntity.ok(ApiResponse.success(data, "Ruta MAPS obtenida"));
    }

    private Map<String, Object> detalleRuta(Certificado certificado, Integer idCarrera, Set<Integer> enRutaIds) {
        List<Map<String, Object>> materias = new ArrayList<>();
        Integer min = null;
        Integer max = null;
        for (CertificadoMateria cm : certificadoMateriaRepository
                .findByIdCertificadoOrderByIdAsc(certificado.getId())) {

            Integer semestre = null;
            if (idCarrera != null) {
                semestre = planEstudiosRepository
                        .findByCarreraIdAndMateriaId(idCarrera, cm.getIdMateria())
                        .stream()
                        .findFirst()
                        .map(PlanEstudios::getSemestre)
                        .orElse(null);
            }
            if (semestre != null) {
                min = min == null ? semestre : Math.min(min, semestre);
                max = max == null ? semestre : Math.max(max, semestre);
            }

            Map<String, Object> materia = new LinkedHashMap<>();
            materia.put("id", cm.getIdMateria());
            materia.put("nombre", materiaRepository.findById(cm.getIdMateria())
                    .map(Materia::getNombre).orElse(null));
            materia.put("semestre", semestre);
            materias.add(materia);
        }

        Map<String, Object> detalle = new LinkedHashMap<>();
        detalle.put("id", certificado.getId());
        detalle.put("nombre", certificado.getNombre());
        detalle.put("descripcion", certificado.getDescripcion());
        detalle.put("semestreMin", min);
        detalle.put("semestreMax", max);
        detalle.put("materias", materias);
        detalle.put("enRuta", enRutaIds.contains(certificado.getId()));
        return detalle;
    }

    @PutMapping("/proposito")
    public ResponseEntity<ApiResponse<Map<String, Object>>> actualizarProposito(
            @Valid @RequestBody PropositoRequestDTO request,
            Authentication authentication) {

        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Estudiante estudiante = estudianteRepository.findByIdUsuario(usuario.getId())
                .orElseThrow(() -> new RuntimeException("El perfil de estudiante aún no está completo"));

        estudiante.setPropositoVida(request.getProposito());
        estudianteRepository.save(estudiante);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("proposito", estudiante.getPropositoVida());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(data, "Propósito de vida actualizado"));
    }

    @PutMapping("/semestre")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> cambiarSemestre(
            @Valid @RequestBody SemestreRequestDTO request,
            Authentication authentication) {

        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Estudiante estudiante = estudianteRepository.findByIdUsuario(usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Completa tu perfil de estudiante antes de cambiar de semestre"));

        if (estudiante.getIdCarrera() == null) {
            throw new IllegalArgumentException(
                    "No se puede actualizar el semestre sin una carrera asignada");
        }

        estudiante.setSemestreActual(request.getSemestre());
        estudianteRepository.save(estudiante);

        estudianteMateriaRepository.deleteByIdEstudiante(estudiante.getId());
        List<MateriaSimpleDTO> materias = new ArrayList<>();
        for (Materia materia : materiaRepository.findMateriasDelSemestre(
                estudiante.getIdCarrera(), request.getSemestre())) {
            estudianteMateriaRepository.save(EstudianteMateria.builder()
                    .idEstudiante(estudiante.getId())
                    .idMateria(materia.getId())
                    .build());
            materias.add(MateriaSimpleDTO.builder()
                    .id(materia.getId())
                    .nombre(materia.getNombre())
                    .build());
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("semestre", estudiante.getSemestreActual());
        data.put("materias", materias);
        data.put("carrera", carreraRepository.findById(estudiante.getIdCarrera())
                .map(Carrera::getNombre)
                .orElse(null));

        return ResponseEntity.ok(ApiResponse.success(data, "Semestre actualizado correctamente"));
    }

}
