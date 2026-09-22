package com.tecmilenio.mapsconect.repository;

import com.tecmilenio.mapsconect.entity.TipAcademico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de tips academicos. Ordena por votos y filtra por materia.
 */
@Repository
public interface TipRepository extends JpaRepository<TipAcademico, Integer> {

    List<TipAcademico> findAllByOrderByTotalVotosDesc();

    List<TipAcademico> findByIdMateriaOrderByTotalVotosDesc(Integer idMateria);

}
