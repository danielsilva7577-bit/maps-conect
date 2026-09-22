package com.tecmilenio.mapsconect.repository;

import com.tecmilenio.mapsconect.entity.VotoTip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio de votos positivos en tips academicos. Verifica existencia y busca por tip y usuario.
 */
@Repository
public interface VotoTipRepository extends JpaRepository<VotoTip, Integer> {

    Optional<VotoTip> findByIdTipAndIdUsuario(Integer idTip, Integer idUsuario);

    boolean existsByIdTipAndIdUsuario(Integer idTip, Integer idUsuario);

}
