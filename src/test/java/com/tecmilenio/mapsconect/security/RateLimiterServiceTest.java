package com.tecmilenio.mapsconect.security;

import com.tecmilenio.mapsconect.exception.TooManyRequestsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests unitarios para RateLimiterService.
 *
 * Reglas bajo prueba:
 *  - Permite hasta maxRequests en la ventana de tiempo
 *  - Bloquea (lanza excepción) al exceder el límite
 *  - Los límites son independientes por scope y por clave
 *  - La ventana de tiempo expira los requests antiguos
 */
class RateLimiterServiceTest {

    private RateLimiterService rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new RateLimiterService();
    }

    @Test
    void permiteHastaMaxRequests() {
        // Act + Assert — 5 requests dentro del límite no lanzan excepción
        for (int i = 0; i < 5; i++) {
            rateLimiter.check("test", "user1", 5, 60);
        }
        // Si llega aquí sin excepción, el test pasa
    }

    @Test
    void bloqueaAlExcederLimite() {
        // Arrange — consume 5 requests (el límite)
        for (int i = 0; i < 5; i++) {
            rateLimiter.check("test", "user1", 5, 60);
        }

        // Act + Assert — el 6º request lanza excepción
        assertThatThrownBy(() -> rateLimiter.check("test", "user1", 5, 60))
                .isInstanceOf(TooManyRequestsException.class)
                .hasMessageContaining("Demasiadas solicitudes");
    }

    @Test
    void limitesIndependientesPorScope() {
        // Arrange — consume el límite en "foro"
        for (int i = 0; i < 10; i++) {
            rateLimiter.check("foro", "user1", 10, 60);
        }

        // Act + Assert — "mensajes" con el mismo user no está bloqueado
        rateLimiter.check("mensajes", "user1", 10, 60);
        // No lanza excepción
    }

    @Test
    void limitesIndependientesPorClave() {
        // Arrange — consume el límite para user1
        for (int i = 0; i < 5; i++) {
            rateLimiter.check("test", "user1", 5, 60);
        }

        // Act + Assert — user2 con el mismo scope no está bloqueado
        rateLimiter.check("test", "user2", 5, 60);
        // No lanza excepción
    }

    @Test
    void permiteNullClave_comoAnonimo() {
        // Act + Assert — clave null no lanza NPE, se trata como "anon"
        rateLimiter.check("test", null, 1, 60);
        // No lanza excepción
    }

    @Test
    void clearScope_reinigiaElBucket() {
        // Arrange — consume el límite
        for (int i = 0; i < 3; i++) {
            rateLimiter.check("test", "user1", 3, 60);
        }
        // El 4º request está bloqueado
        assertThatThrownBy(() -> rateLimiter.check("test", "user1", 3, 60))
                .isInstanceOf(TooManyRequestsException.class);

        // Act — limpiar el scope
        rateLimiter.clearScope("test");

        // Assert — después de limpiar, el request es permitido
        rateLimiter.check("test", "user1", 3, 60);
    }
}
