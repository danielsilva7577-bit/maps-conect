package com.tecmilenio.mapsconect.repository;

import com.tecmilenio.mapsconect.entity.ProfesorMateria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfesorMateriaRepository extends JpaRepository<ProfesorMateria, Integer> {

    List<ProfesorMateria> findByIdProfesor(Integer idProfesor);

    void deleteByIdProfesor(Integer idProfesor);

    boolean existsByIdProfesorAndIdMateria(Integer idProfesor, Integer idMateria);

}