package com.tecmilenio.mapsconect.repository;

import com.tecmilenio.mapsconect.entity.Conversacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de conversaciones de chat. Busca conversaciones por participante y ordena por fecha de inicio.
 */
@Repository
public interface ConversacionRepository extends JpaRepository<Conversacion, Integer> {

    List<Conversacion> findByUsuario1IdOrUsuario2IdOrderByFechaInicioDesc(Integer id1, Integer id2);

}
