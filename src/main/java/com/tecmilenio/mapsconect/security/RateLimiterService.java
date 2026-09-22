package com.tecmilenio.mapsconect.security;

import com.tecmilenio.mapsconect.exception.TooManyRequestsException;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiter genérico basado en ventana deslizente (sliding window).
 *
 * <p>Soporta múltiples "scopes" (categorías) con límites y ventanas independientes.
 * La clave de límite por defecto es el email del usuario autenticado; si no
 * hay autenticación, se usa la IP del cliente.</p>
 *
 * <p>Este componente reemplaza y generaliza a {@link LoginRateLimiter}, manteniendo
 * el mismo algoritmo de ventana deslizente pero con configuración por scope.
 * Se invoca desde {@link RateLimitingInterceptor} para endpoints anotados con
 * {@link RateLimited}.</p>
 *
 * <p>Uso típico:</p>
 * <pre>{@code
 * rateLimiterService.check("mensajes", email, 30, 60);  // 30 req / 60 s
 * }</pre>
 */
@Component
public class RateLimiterService {

    /**
     * Buckets por scope → clave (email/IP) → historial de timestamps.
     * Se usa ConcurrentHashMap para thread-safety sin sincronización explícita.
     */
    private final Map<String, Map<String, Deque<Long>>> buckets = new ConcurrentHashMap<>();

    /**
     * Verifica si la clave puede ejecutar la acción dentro del scope.
     *
     * <p>Algoritmo de ventana deslizente:</p>
     * <ol>
     *   <li>Obtiene (o crea) el bucket para el scope y la clave.</li>
     *   <li>Elimina timestamps que están fuera de la ventana actual.</li>
     *   <li>Si el número de timestamps restantes supera el límite, lanza
     *       {@link TooManyRequestsException} con el tiempo restante.</li>
     *   <li>Si está dentro del límite, agrega el timestamp actual y continúa.</li>
     * </ol>
     *
     * @param scope         categoría de rate limiting (ej. "mensajes", "foro", "upload")
     * @param key           identificador del usuario/IP
     * @param maxReq        cantidad máxima de requests permitidos en la ventana
     * @param windowSegundos tamaño de la ventana en segundos
     * @throws TooManyRequestsException si se excede el límite
     */
    public void check(String scope, String key, int maxReq, long windowSegundos) {
        if (key == null || key.isBlank()) {
            key = "anon";
        }

        long ahora = System.currentTimeMillis();
        long windowMs = windowSegundos * 1000L;

        Map<String, Deque<Long>> scopeBuckets = buckets.computeIfAbsent(scope, k -> new ConcurrentHashMap<>());
        Deque<Long> historial = scopeBuckets.computeIfAbsent(key, k -> new ArrayDeque<>());

        synchronized (historial) {
            // Elimina timestamps fuera de la ventana
            while (!historial.isEmpty() && ahora - historial.peek() > windowMs) {
                historial.poll();
            }

            if (historial.size() >= maxReq) {
                long restante = windowMs - (ahora - historial.peek());
                throw new TooManyRequestsException(
                        "Demasiadas solicitudes. Intenta de nuevo en " + Math.max(1, restante / 1000L) + " segundos.");
            }

            historial.add(ahora);
        }
    }

    /**
     * Limpia todos los buckets de un scope (útil para testing y para forzar reset).
     *
     * @param scope categoría a limpiar
     */
    public void clearScope(String scope) {
        buckets.remove(scope);
    }

    /**
     * Limpia todos los buckets de todos los scopes.
     */
    public void clearAll() {
        buckets.clear();
    }

}


