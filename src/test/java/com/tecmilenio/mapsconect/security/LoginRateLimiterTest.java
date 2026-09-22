package com.tecmilenio.mapsconect.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para LoginRateLimiter — protección contra fuerza bruta en login.
 *
 * Reglas bajo prueba:
 *  - 5 intentos fallidos en 15 min → bloquea temporalmente
 *  - 1 éxito → limpia el historial de fallos
 *  - Ventana de 15 min expira los fallos antiguos
 */
class LoginRateLimiterTest {

    private LoginRateLimiter rateLimiter;
    private final String CLAVE = "test@correo.tecmilenio.mx";

    @BeforeEach
    void setUp() {
        rateLimiter = new LoginRateLimiter();
    }

    @Test
    void noBloqueado_cuandoNoHayFallos() {
        // Act
        Long bloqueo = rateLimiter.tiempoRestanteBloqueo(CLAVE);

        // Assert
        assertThat(bloqueo).isNull();
    }

    @Test
    void noBloqueado_despuesDeMenosDe5Fallos() {
        // Act — 4 fallos consecutivos
        for (int i = 0; i < 4; i++) {
            rateLimiter.registrarFallo(CLAVE);
        }

        // Assert
        assertThat(rateLimiter.tiempoRestanteBloqueo(CLAVE)).isNull();
    }

    @Test
    void bloqueado_despuesDe5Fallos() {
        // Act — 5 fallos consecutivos (el límite)
        for (int i = 0; i < 5; i++) {
            rateLimiter.registrarFallo(CLAVE);
        }

        // Assert
        Long restante = rateLimiter.tiempoRestanteBloqueo(CLAVE);
        assertThat(restante).isNotNull();
        // La ventana es de 15 min = 900 seg, y el primer fallo fue hace casi 0 ms,
        // así que quedan ~899-900 segundos.
        assertThat(restante).isGreaterThan(890).isLessThanOrEqualTo(900);
    }

    @Test
    void loginExitoso_limpiaFallosYDesbloquea() {
        // Arrange — 4 fallos (cerca del límite)
        for (int i = 0; i < 4; i++) {
            rateLimiter.registrarFallo(CLAVE);
        }

        // Act — éxito
        rateLimiter.registrarExito(CLAVE);

        // Assert
        assertThat(rateLimiter.tiempoRestanteBloqueo(CLAVE)).isNull();
    }

    @Test
    void bloqueoPersistencia_entreClaves_independientes() {
        // Arrange
        String claveA = "a@correo.tecmilenio.mx";
        String claveB = "b@correo.tecmilenio.mx";

        // Act — 5 fallos en A, 2 fallos en B
        for (int i = 0; i < 5; i++) {
            rateLimiter.registrarFallo(claveA);
        }
        rateLimiter.registrarFallo(claveB);
        rateLimiter.registrarFallo(claveB);

        // Assert — B no está bloqueada
        assertThat(rateLimiter.tiempoRestanteBloqueo(claveB)).isNull();
        // A está bloqueada
        assertThat(rateLimiter.tiempoRestanteBloqueo(claveA)).isNotNull();
    }
}
