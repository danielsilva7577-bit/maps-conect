package com.tecmilenio.mapsconect.repository;

import com.tecmilenio.mapsconect.entity.Materia;
import com.tecmilenio.mapsconect.entity.PlanEstudios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MateriaRepository extends JpaRepository<Materia, Integer> {

    @Query("SELECT p.materia FROM PlanEstudios p WHERE p.carrera.id = ?1 AND p.semestre = ?2 ORDER BY p.materia.nombre")
    List<Materia> findMateriasDelSemestre(Integer idCarrera, Integer semestre);

    @Query("SELECT p.materia FROM PlanEstudios p WHERE p.carrera.id = ?1 ORDER BY p.materia.nombre")
    List<Materia> findMateriasDeCarrera(Integer idCarrera);

    @Query("SELECT m FROM Materia m ORDER BY m.nombre")
    List<Materia> findAllOrderedByNombre();

    List<Materia> findTop5ByNombreContainingIgnoreCaseOrderByNombreAsc(String nombre);

}