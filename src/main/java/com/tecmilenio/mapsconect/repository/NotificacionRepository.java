package com.tecmilenio.mapsconect.repository;

import com.tecmilenio.mapsconect.entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repositorio de notificaciones. Consultas para listar notificaciones por usuario, contar no leidas y marcar todas como leidas.
 */
@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {

    List<Notificacion> findTop30ByIdUsuarioOrderByFechaCreacionDesc(Integer idUsuario);

    long countByIdUsuarioAndLeidaFalse(Integer idUsuario);

    @Transactional
    @Modifying
    @Query("UPDATE Notificacion n SET n.leida = true WHERE n.idUsuario = :idUsuario AND n.leida = false")
    int marcarTodasLeidas(@Param("idUsuario") Integer idUsuario);

    @Transactional
    @Modifying
    @Query("DELETE FROM Notificacion n WHERE n.idUsuario = :idUsuario")
    int eliminarTodas(@Param("idUsuario") Integer idUsuario);

}

