package com.tecmilenio.mapsconect.controller;

import com.tecmilenio.mapsconect.dto.ApiResponse;
import com.tecmilenio.mapsconect.dto.CarreraDTO;
import com.tecmilenio.mapsconect.entity.Carrera;
import com.tecmilenio.mapsconect.repository.CarreraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador de carreras académicas.
 *
 * <p>Expone endpoints públicos para listar las carreras disponibles en la
 * plataforma. Las carreras son el eje organizador del plan de estudios y
 * determinan qué materias vemos en el foro y en la bolsa de empleo.</p>
 *
 * <p>No requiere autenticación.</p>
 */
@RestController
@RequestMapping("/carreras")
public class CarreraController {

    @Autowired
    private CarreraRepository carreraRepository;

    /**
     * Lista todas las carreras activas ordenadas alfabéticamente.
     *
     * @return 200 OK con la lista de carreras
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CarreraDTO>>> listarActivas() {
        List<CarreraDTO> carreras = carreraRepository.findByActivaTrueOrderByNombreAsc().stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(carreras, "Carreras obtenidas"));
    }

    /**
     * Convierte una entidad {@link Carrera} a su DTO de transferencia.
     *
     * @param carrera entidad origen
     * @return DTO con id, nombre, clave y estado de la carrera
     */
    private CarreraDTO mapearADTO(Carrera carrera) {
        return CarreraDTO.builder()
                .id(carrera.getId())
                .nombre(carrera.getNombre())
                .clave(carrera.getClave())
                .activa(carrera.getActiva())
                .build();
    }

}
