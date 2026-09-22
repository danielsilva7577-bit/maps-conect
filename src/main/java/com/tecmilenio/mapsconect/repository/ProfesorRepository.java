package com.tecmilenio.mapsconect.repository;

import com.tecmilenio.mapsconect.entity.Profesor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfesorRepository extends JpaRepository<Profesor, Integer> {

    Optional<Profesor> findByIdUsuario(Integer idUsuario);

    boolean existsByIdUsuario(Integer idUsuario);

    boolean existsByNumeroNomina(String numeroNomina);

}