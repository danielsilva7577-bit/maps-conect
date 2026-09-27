package com.tecmilenio.mapsconect.repository;

import com.tecmilenio.mapsconect.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repositorio de refresh tokens opacos.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    /** Revoca todos los refresh tokens de un usuario (logout global). */
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revocado = true WHERE r.idUsuario = :idUsuario")
    void revocarTodosPorUsuario(@Param("idUsuario") Integer idUsuario);

    /** Limpieza periódica: elimina tokens expirados o revocados. */
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.fechaExpira < :ahora OR r.revocado = true")
    void eliminarExpiradosYRevocados(@Param("ahora") LocalDateTime ahora);
}
