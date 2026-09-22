package com.tecmilenio.mapsconect.repository;

import com.tecmilenio.mapsconect.entity.EstudianteMateria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de inscripciones de materias por estudiante.
 */
@Repository
public interface EstudianteMateriaRepository extends JpaRepository<EstudianteMateria, Integer> {

    void deleteByIdEstudiante(Integer idEstudiante);

    List<EstudianteMateria> findByIdEstudiante(Integer idEstudiante);

    boolean existsByIdEstudianteAndIdMateria(Integer idEstudiante, Integer idMateria);

}

