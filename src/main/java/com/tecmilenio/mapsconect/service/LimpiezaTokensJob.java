package com.tecmilenio.mapsconect.service;

import com.tecmilenio.mapsconect.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Job programado de limpieza de refresh tokens.
 *
 * <p>Elimina diariamente (a las 03:00 AM) todos los refresh tokens que hayan
 * expirado o hayan sido revocados, evitando que la tabla {@code refresh_tokens}
 * crezca indefinidamente.</p>
 *
 * <p>Requiere que {@code @EnableScheduling} esté presente en la clase de
 * arranque ({@link com.tecmilenio.mapsconect.MapsConectApplication}).</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LimpiezaTokensJob {

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Elimina refresh tokens expirados o revocados.
     *
     * <p>Se ejecuta todos los días a las 03:00 AM (hora del servidor).
     * La expresión cron {@code "0 0 3 * * *"} significa:
     * segundo 0, minuto 0, hora 3, cualquier día/mes/día-semana.</p>
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void limpiarTokensExpiradosYRevocados() {
        LocalDateTime ahora = LocalDateTime.now();
        log.info("[LimpiezaTokens] Iniciando limpieza de refresh tokens expirados/revocados...");
        try {
            refreshTokenRepository.eliminarExpiradosYRevocados(ahora);
            log.info("[LimpiezaTokens] Limpieza completada correctamente.");
        } catch (Exception e) {
            log.error("[LimpiezaTokens] Error durante la limpieza de tokens: {}", e.getMessage(), e);
        }
    }
}
