package com.tecmilenio.mapsconect.repository;

import com.tecmilenio.mapsconect.entity.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de mensajes. Consultas para listar mensajes por conversacion, contar no leidos y obtener el ultimo mensaje.
 */
@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Integer> {

    List<Mensaje> findByIdConversacionOrderByIdAsc(Integer idConversacion);

    Optional<Mensaje> findFirstByIdConversacionOrderByIdDesc(Integer idConversacion);

    @Query("SELECT COUNT(m) FROM Mensaje m WHERE m.idConversacion = :idConversacion AND m.idEmisor <> :idUsuario AND m.leido = false")
    long countNoLeidos(@Param("idConversacion") Integer idConversacion, @Param("idUsuario") Integer idUsuario);

    @Query("SELECT m.idConversacion, COUNT(m) FROM Mensaje m WHERE m.idEmisor <> :idUsuario AND m.leido = false GROUP BY m.idConversacion")
    List<Object[]> resumenNoLeidos(@Param("idUsuario") Integer idUsuario);

}
