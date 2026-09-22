package com.tecmilenio.mapsconect.repository;

import com.tecmilenio.mapsconect.entity.Seguimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repositorio Spring Data JPA para la entidad SeguimientoRepository.
 */
@Repository
public interface SeguimientoRepository extends JpaRepository<Seguimiento, Integer> {

    boolean existsByIdSeguidorAndIdSeguido(Integer idSeguidor, Integer idSeguido);

    @Transactional
    void deleteByIdSeguidorAndIdSeguido(Integer idSeguidor, Integer idSeguido);

    long countByIdSeguido(Integer idSeguido);

    long countByIdSeguidor(Integer idSeguidor);

}
