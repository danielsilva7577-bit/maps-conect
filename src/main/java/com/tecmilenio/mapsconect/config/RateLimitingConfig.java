package com.tecmilenio.mapsconect.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.tecmilenio.mapsconect.security.RateLimitingProperties;

/**
 * Registro de propiedades de rate limiting (app.rate-limiting.*).
 *
 * <p>Habilita el binding de type-safe properties para
 * {@link RateLimitingProperties} mediante {@link EnableConfigurationProperties},
 * permitiendo que los valores configurados en {@code application.yml} bajo
 * el prefijo {@code app.rate-limiting} se inyecten como un record inmutable.</p>
 *
 * <p>Ejemplo:</p>
 * <pre>{@code
 * app:
 *   rate-limiting:
 *     default:
 *       max-requests: 100
 *       window-seconds: 60
 * }</pre>
 *
 * @see RateLimitingProperties
 * @see com.tecmilenio.mapsconect.security.RateLimitingInterceptor
 */
@Configuration
@EnableConfigurationProperties(RateLimitingProperties.class)
public class RateLimitingConfig {
}


