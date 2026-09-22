package com.tecmilenio.mapsconect.controller;

import com.tecmilenio.mapsconect.dto.ApiResponse;
import com.tecmilenio.mapsconect.dto.MateriaSimpleDTO;
import com.tecmilenio.mapsconect.entity.Materia;
import com.tecmilenio.mapsconect.repository.MateriaRepository;
import com.tecmilenio.mapsconect.service.CarreraContextoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador de materias académicas.
 *
 * <p>Expone endpoints para listar materias. Si el usuario está autenticado
 * y tiene carrera asignada, el listado se filtra por las materias de su
 * plan de estudios; de lo contrario, se muestran todas las materias.</p>
 *
 * <p>También permite consultar las materias de un semestre específico
 * dentro de un plan de estudios de carrera.</p>
 */
@RestController
@RequestMapping("/materias")
public class MateriaController {

    @Autowired
    private MateriaRepository materiaRepository;

    @Autowired
    private CarreraContextoService carreraContextoService;

    /**
     * Lista las materias disponibles.
     *
     * <p>Si el usuario autenticado tiene una carrera registrada, se filtran
     * las materias de ese plan de estudios. Si no está autenticado o no
     * tiene carrera, se listan todas las materias.</p>
     *
     * @param authentication usuario autenticado (puede ser nulo)
     * @return 200 OK con la lista de materias
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<MateriaSimpleDTO>>> listarTodas(Authentication authentication) {
        Integer idCarrera = authentication == null
                ? null
                : carreraContextoService.idCarreraDeUsuario(authentication.getName());

        List<MateriaSimpleDTO> materias = (idCarrera == null
                ? materiaRepository.findAllOrderedByNombre()
                : materiaRepository.findMateriasDeCarrera(idCarrera))
                .stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(materias, "Materias obtenidas"));
    }

    /**
     * Obtiene las materias de un semestre específico de un plan de estudios.
     *
     * @param idCarrera ID de la carrera
     * @param semestre  número de semestre (1, 2, 3, ...)
     * @return 200 OK con las materias del semestre solicitado
     */
    @GetMapping("/plan/{idCarrera}/{semestre}")
    public ResponseEntity<ApiResponse<List<MateriaSimpleDTO>>> materiasDelSemestre(
            @PathVariable Integer idCarrera,
            @PathVariable Integer semestre) {

        List<MateriaSimpleDTO> materias = materiaRepository.findMateriasDelSemestre(idCarrera, semestre)
                .stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(materias, "Materias del semestre obtenidas"));
    }

    /**
     * Convierte una entidad {@link Materia} a su DTO simple.
     *
     * @param materia entidad origen
     * @return DTO con id y nombre de la materia
     */
    private MateriaSimpleDTO mapearADTO(Materia materia) {
        return MateriaSimpleDTO.builder()
                .id(materia.getId())
                .nombre(materia.getNombre())
                .build();
    }

}
