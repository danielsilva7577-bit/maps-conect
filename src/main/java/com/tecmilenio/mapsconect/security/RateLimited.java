package com.tecmilenio.mapsconect.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación declarativa para rate limiting en endpoints sensibles.
 *
 * <p>Se coloca sobre métodos de controlador o sobre toda una clase. El
 * {@link RateLimitingInterceptor} la detecta en tiempo de ejecución y
 * aplica el límite correspondiente a través de {@link RateLimiterService}.</p>
 *
 * <p>Los valores de maxRequests y windowSeconds son opcionales: si se dejan
 * en 0 (el default), se usan los valores configurados en
 * {@code app.rate-limiting.<scope>} de application.yml. Si el scope tampoco
 * está configurado, se usan los defaults genéricos (30 req / 60 s).</p>
 *
 * <p>Ejemplo — usa configuración de application.yml:</p>
 * <pre>{@code
 * @RateLimited(scope = "foro")
 * }</pre>
 *
 * <p>Ejemplo — override explícito:</p>
 * <pre>{@code
 * @RateLimited(scope = "uploads", maxRequests = 3, windowSeconds = 30)
 * }</pre>
 *
 * @see RateLimitingInterceptor
 * @see RateLimitingProperties
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {

    /**
     * Categoría de rate limiting (clave del bucket en RateLimiterService).
     * Se usa como prefijo en la configuración de application.yml:
     * {@code app.rate-limiting.<scope>}.
     */
    String scope();

    /**
     * Número máximo de requests permitidos en la ventana.
     * {@code 0} (default) = usar configuración de application.yml o el default genérico.
     */
    int maxRequests() default 0;

    /**
     * Tamaño de la ventana en segundos.
     * {@code 0} (default) = usar configuración de application.yml o el default genérico.
     */
    long windowSeconds() default 0;
}
