package com.tecmilenio.mapsconect.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.Collections;
import java.util.Map;

/**
 * Propiedades de rate limiting configurables desde application.yml.
 *
 * <p>Se enlazan a las propiedades con prefijo {@code app.rate-limiting}
 * mediante {@link ConstructorBinding} (inmutables, type-safe).</p>
 *
 * <p>Ejemplo de configuración:</p>
 * <pre>{@code
 * app:
 *   rate-limiting:
 *     foro:
 *       max-requests: 10
 *       window-seconds: 60
 *     mensajes:
 *       max-requests: 60
 *       window-seconds: 60
 *     default:
 *       max-requests: 30
 *       window-seconds: 60
 * }</pre>
 *
 * <p>Si un scope no está configurado, se usan los defaults genéricos
 * definidos en {@link #DEFAULT_LIMIT} (30 req / 60 s).</p>
 *
 * @param scopes   mapa de scope → límite (clave "default" opcional)
 * @param defaults límite genérico usado cuando el scope no está en {@code scopes}
 */
@ConfigurationProperties(prefix = "app.rate-limiting")
public record RateLimitingProperties(
        Map<String, Limit> scopes,
        Limit defaults
) {

    /**
     * Límite individual: cantidad máxima de requests y ventana en segundos.
     *
     * @param maxRequests   número máximo de requests permitidos
     * @param windowSeconds tamaño de la ventana en segundos
     */
    public record Limit(int maxRequests, long windowSeconds) {
    }

    /** Default para scopes sin configuración explícita (30 req / 60 s). */
    public static final Limit DEFAULT_LIMIT = new Limit(30, 60);

    /**
     * Resuelve el límite para un scope específico.
     *
     * <p>Si {@code scopes} es nulo o no contiene el scope solicitado,
     * devuelve {@link #defaults} si está configurado, o {@link #DEFAULT_LIMIT}
     * como último recurso.</p>
     *
     * @param scope categoría solicitada (ej. "mensajes", "foro", "uploads")
     * @return límite configurado para el scope (nunca {@code null})
     */
    public Limit limitFor(String scope) {
        if (scopes == null) {
            return defaults != null ? defaults : DEFAULT_LIMIT;
        }
        Limit limit = scopes.get(scope);
        if (limit == null) {
            return defaults != null ? defaults : DEFAULT_LIMIT;
        }
        return limit;
    }

    /**
     * Crea una instancia vacía (sin scopes, sin defaults) para uso en tests.
     *
     * @return instancia con mapa vacío y defaults nulos
     */
    public static RateLimitingProperties empty() {
        return new RateLimitingProperties(Collections.emptyMap(), null);
    }
}
