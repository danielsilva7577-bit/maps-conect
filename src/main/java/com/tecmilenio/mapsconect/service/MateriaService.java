package com.tecmilenio.mapsconect.service;

import com.tecmilenio.mapsconect.dto.MateriaDTO;
import com.tecmilenio.mapsconect.entity.Materia;
import com.tecmilenio.mapsconect.exception.ResourceNotFoundException;
import com.tecmilenio.mapsconect.repository.MateriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MateriaService {

    @Autowired
    private MateriaRepository materiaRepository;

    public List<MateriaDTO> listar(String tipo) {
        List<Materia> materias;
        if (tipo != null && !tipo.isBlank()) {
            Materia.Tipo tipoEnum;
            try {
                tipoEnum = Materia.Tipo.valueOf(tipo.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Tipo de materia inválido: " + tipo);
            }
            materias = materiaRepository.findByTipo(tipoEnum);
        } else {
            materias = materiaRepository.findAll();
        }
        return materias.stream().map(this::mapearADTO).toList();
    }

    public MateriaDTO obtenerPorId(Integer id) {
        Materia materia = materiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Materia no encontrada"));
        return mapearADTO(materia);
    }

    private MateriaDTO mapearADTO(Materia materia) {
        return MateriaDTO.builder()
                .id(materia.getId())
                .clave(materia.getClave())
                .nombre(materia.getNombre())
                .creditos(materia.getCreditos())
                .tipo(materia.getTipo() != null ? materia.getTipo().name() : null)
                .build();
    }

}
