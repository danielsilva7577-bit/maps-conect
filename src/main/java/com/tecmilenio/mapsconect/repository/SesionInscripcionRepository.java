package com.tecmilenio.mapsconect.repository;

import com.tecmilenio.mapsconect.entity.SesionInscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface SesionInscripcionRepository extends JpaRepository<SesionInscripcion, SesionInscripcion.SesionInscripcionId> {

    @Query("SELECT COUNT(s) > 0 FROM SesionInscripcion s WHERE s.id.idSesion = :idSesion AND s.id.idUsuario = :idUsuario")
    boolean existe(@Param("idSesion") Integer idSesion, @Param("idUsuario") Integer idUsuario);

    @Query("SELECT COUNT(s) FROM SesionInscripcion s WHERE s.id.idSesion = :idSesion")
    long contar(@Param("idSesion") Integer idSesion);

    @Modifying
    @Transactional
    @Query("DELETE FROM SesionInscripcion s WHERE s.id.idSesion = :idSesion AND s.id.idUsuario = :idUsuario")
    void eliminar(@Param("idSesion") Integer idSesion, @Param("idUsuario") Integer idUsuario);
}