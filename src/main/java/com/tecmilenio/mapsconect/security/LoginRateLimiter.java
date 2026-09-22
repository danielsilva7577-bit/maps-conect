package com.tecmilenio.mapsconect.security;

import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Protección básica contra fuerza bruta en el login (endpoint /auth/login).
 *
 * <p>Registra intentos fallidos por credencial o IP dentro de una ventana de
 * tiempo. Cuando el número de fallos supera {@link #MAX_INTENTOS}, la credencial
 * queda bloqueada temporalmente hasta que transcurre la ventana.</p>
 *
 * <p>Esta es la protección más simple y está dedicada exclusivamente al login.
 * Para el resto de endpoints se usa el mecanismo más general
 * {@link RateLimiterService} + anotación {@link RateLimited}.</p>
 */
@Component
public class LoginRateLimiter {

    /** Número máximo de intentos fallidos permitidos antes de bloquear. */
    private static final int MAX_INTENTOS = 5;

    /** Ventana de tiempo en milisegundos (15 minutos) para contar los fallos. */
    private static final long VENTANA_MS = 15 * 60 * 1000L;

    /**
     * Buckets por clave (email o IP) → historial de timestamps de intentos fallidos.
     * Se usa ConcurrentHashMap para thread-safety sin sincronización explícita.
     */
    private final Map<String, Deque<Long>> fallos = new ConcurrentHashMap<>();

    /**
     * Registra un intento fallido de login para la clave dada.
     *
     * <p>Limpia los timestamps antiguos (fuera de la ventana) antes de
     * agregar el nuevo, manteniendo la ventana deslizante.</p>
     *
     * @param clave email o IP del cliente que intentó loguearse
     */
    public void registrarFallo(String clave) {
        long ahora = System.currentTimeMillis();
        fallos.compute(clave, (k, cola) -> {
            Deque<Long> d = cola == null ? new ArrayDeque<>() : cola;
            // Elimina timestamps que ya salieron de la ventana
            while (!d.isEmpty() && ahora - d.peek() > VENTANA_MS) {
                d.poll();
            }
            d.add(ahora);
            return d;
        });
    }

    /**
     * Limpia el historial de fallos para una clave tras un login exitoso.
     *
     * @param clave email o IP del usuario que autenticó correctamente
     */
    public void registrarExito(String clave) {
        fallos.remove(clave);
    }

    /**
     * Calcula el tiempo restante de bloqueo para una clave.
     *
     * <p>Si la clave tiene menos de {@link #MAX_INTENTOS} fallos en la
     * ventana actual, no está bloqueada y devuelve {@code null}.</p>
     *
     * @param clave email o IP a consultar
     * @return segundos restantes de bloqueo, o {@code null} si está libre
     */
    public Long tiempoRestanteBloqueo(String clave) {
        long ahora = System.currentTimeMillis();
        Deque<Long> d = fallos.get(clave);
        if (d == null || d.isEmpty()) {
            return null;
        }
        synchronized (d) {
            // Limpia timestamps expirados
            d.removeIf(t -> ahora - t > VENTANA_MS);
            if (d.isEmpty()) {
                fallos.remove(clave, d);
                return null;
            }
            if (d.size() < MAX_INTENTOS) {
                return null; // Aún hay espacio para más intentos
            }
            // El bloqueo dura desde el primer fallo hasta que la ventana expire
            Long primerFallo = d.peek();
            if (primerFallo == null) {
                return null;
            }
            long restante = VENTANA_MS - (ahora - primerFallo);
            return Math.max(1, restante / 1000L);
        }
    }

    /**
     * Limpia el historial de fallos (package-private, usado en tests).
     *
     * @param clave email o IP a limpiar
     */
    void limpiar(String clave) {
        fallos.remove(clave);
    }
}


