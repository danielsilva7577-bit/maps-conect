package com.tecmilenio.mapsconect.repository;

import com.tecmilenio.mapsconect.entity.Mensaje;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de mensajes. Consultas para listar mensajes por conversacion,
 * contar no leidos, obtener el ultimo mensaje y marcar como leidos en bloque.
 */
@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Integer> {

    /** Todos los mensajes de una conversación (usado internamente por marcarLeidos). */
    List<Mensaje> findByIdConversacionOrderByIdAsc(Integer idConversacion);

    /**
     * Mensajes paginados de una conversación, ordenados cronológicamente.
     * Usado por obtenerDetalle() para no cargar toda la conversación en RAM.
     */
    Page<Mensaje> findByIdConversacionOrderByIdAsc(Integer idConversacion, Pageable pageable);

    Optional<Mensaje> findFirstByIdConversacionOrderByIdDesc(Integer idConversacion);

    @Query("SELECT COUNT(m) FROM Mensaje m WHERE m.idConversacion = :idConversacion AND m.idEmisor <> :idUsuario AND m.leido = false")
    long countNoLeidos(@Param("idConversacion") Integer idConversacion, @Param("idUsuario") Integer idUsuario);

    @Query("SELECT m.idConversacion, COUNT(m) FROM Mensaje m WHERE m.idEmisor <> :idUsuario AND m.leido = false GROUP BY m.idConversacion")
    List<Object[]> resumenNoLeidos(@Param("idUsuario") Integer idUsuario);

    /**
     * Marca como leídos todos los mensajes de una conversación que no fueron
     * enviados por el usuario dado. UPDATE directo en BD — evita cargar la
     * lista entera en RAM y es atómico dentro de la transacción del llamador.
     */
    @Modifying
    @Query("UPDATE Mensaje m SET m.leido = true " +
           "WHERE m.idConversacion = :idConversacion " +
           "AND m.idEmisor <> :idUsuario " +
           "AND m.leido = false")
    void marcarLeidosMasivo(@Param("idConversacion") Integer idConversacion,
                            @Param("idUsuario") Integer idUsuario);

}
