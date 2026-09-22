package com.tecmilenio.mapsconect.repository;

import com.tecmilenio.mapsconect.entity.PlanEstudios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanEstudiosRepository extends JpaRepository<PlanEstudios, Integer> {

    List<PlanEstudios> findByCarreraId(Integer idCarrera);

    List<PlanEstudios> findByCarreraIdAndSemestre(Integer idCarrera, Integer semestre);

    List<PlanEstudios> findByCarreraIdAndMateriaId(Integer idCarrera, Integer idMateria);

    boolean existsByCarreraIdAndSemestre(Integer idCarrera, Integer semestre);

}