package com.tecmilenio.mapsconect.repository;

import com.tecmilenio.mapsconect.entity.Conversacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de conversaciones de chat.
 * Busca conversaciones por participante y ordena por fecha de inicio descendente.
 */
@Repository
public interface ConversacionRepository extends JpaRepository<Conversacion, Integer> {

    /** Sin paginar — usado internamente cuando se necesita la lista completa. */
    List<Conversacion> findByUsuario1IdOrUsuario2IdOrderByFechaInicioDesc(Integer id1, Integer id2);

    /**
     * Paginado — usado por listarConversaciones() para no cargar toda la
     * historia de un usuario en RAM de una sola vez.
     */
    Page<Conversacion> findByUsuario1IdOrUsuario2IdOrderByFechaInicioDesc(Integer id1, Integer id2, Pageable pageable);

}
