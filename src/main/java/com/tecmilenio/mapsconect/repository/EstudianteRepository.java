package com.tecmilenio.mapsconect.repository;

import com.tecmilenio.mapsconect.entity.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstudianteRepository extends JpaRepository<Estudiante, Integer> {

    Optional<Estudiante> findByUsuario_Id(Integer idUsuario);

    boolean existsByMatricula(String matricula);

}
