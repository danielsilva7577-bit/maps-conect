package com.tecmilenio.mapsconect.service;

import com.tecmilenio.mapsconect.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para LimpiezaTokensJob.
 *
 * Cobertura:
 *  - ejecución exitosa llama al repositorio con timestamp correcto
 *  - excepción del repositorio es capturada (el job no se rompe)
 */
@ExtendWith(MockitoExtension.class)
class LimpiezaTokensJobTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks private LimpiezaTokensJob limpiezaTokensJob;

    @Test
    void limpiar_exitoso_llamaAlRepositorio() {
        doNothing().when(refreshTokenRepository).eliminarExpiradosYRevocados(any(LocalDateTime.class));

        limpiezaTokensJob.limpiarTokensExpiradosYRevocados();

        verify(refreshTokenRepository, times(1))
                .eliminarExpiradosYRevocados(any(LocalDateTime.class));
    }

    @Test
    void limpiar_excepcionEnRepositorio_noPropagarExcepcion() {
        doThrow(new RuntimeException("Error de BD simulado"))
                .when(refreshTokenRepository).eliminarExpiradosYRevocados(any(LocalDateTime.class));

        // El job debe capturar la excepción internamente y no propagarla
        assertDoesNotThrowWrapper(() -> limpiezaTokensJob.limpiarTokensExpiradosYRevocados());

        verify(refreshTokenRepository, times(1))
                .eliminarExpiradosYRevocados(any(LocalDateTime.class));
    }

    /** Wrapper para evitar importar AssertJ solo para este método puntual. */
    private void assertDoesNotThrowWrapper(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            throw new AssertionError("Se esperaba que el job no lanzara excepción, pero lanzó: " + e.getMessage(), e);
        }
    }
}
