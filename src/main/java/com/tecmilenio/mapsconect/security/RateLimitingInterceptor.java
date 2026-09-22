package com.tecmilenio.mapsconect.security;

import com.tecmilenio.mapsconect.exception.TooManyRequestsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodIntrospector;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

/**
 * Interceptor que aplica rate limiting declarativo a controladores
 * anotados con {@link RateLimited}.
 *
 * <p>Si la anotación no especifica maxRequests/windowSeconds (valor 0),
 * se usan los valores configurados en {@code app.rate-limiting.<scope>}
 * desde application.yml, o el default genérico (30 req / 60 s).</p>
 *
 * <p>La clave de límite por defecto es el email del usuario autenticado;
 * si no hay autenticación, se usa la IP del cliente.</p>
 *
 * <p>Este interceptor se registra globalmente a través de
 * {@link com.tecmilenio.mapsconect.config.CorsConfig#addInterceptors}.</p>
 */
@Component
public class RateLimitingInterceptor implements HandlerInterceptor {

    /** Servicio que lleva la cuenta de requests en memoria (ventana deslizente). */
    private final RateLimiterService rateLimiterService;

    /** Propiedades configurables para límites por scope. */
    private final RateLimitingProperties properties;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param rateLimiterService servicio de rate limiting en memoria
     * @param properties         configuración de límites desde application.yml
     */
    @Autowired
    public RateLimitingInterceptor(RateLimiterService rateLimiterService,
                                   RateLimitingProperties properties) {
        this.rateLimiterService = rateLimiterService;
        this.properties = properties;
    }

    /**
     * Antes de ejecutar cada controlador, verifica el rate limiting si el
     * handler está anotado con {@link RateLimited}.
     *
     * <p>Flujo:</p>
     * <ol>
     *   <li>Si el handler no es un método de controlador, permite el request.</li>
     *   <li>Busca la anotación {@link RateLimited} (en el método o en la clase).</li>
     *   <li>Si no hay anotación, permite el request.</li>
     *   <li>Resuelve el scope y los límites (de la anotación o de application.yml).</li>
     *   <li>Resuelve la clave (email del usuario o IP del cliente).</li>
     *   <li>Delega a {@link RateLimiterService#check()} que lanza
     *       {@link TooManyRequestsException} si se excede el límite.</li>
     * </ol>
     *
     * @param request   objeto {@link HttpServletRequest}
     * @param response  objeto {@link HttpServletResponse}
     * @param handler   handler que procesará el request
     * @return {@code true} para continuar la cadena, nunca retorna {@code false}
     * @throws TooManyRequestsException si el rate limit es excedido
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws TooManyRequestsException {

        // Los filtros de Spring Security pueden envolver el handler; asegúrate
        // de que sea un HandlerMethod (método de un @RestController).
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // Busca la anotación @RateLimited en el método o, por herencia, en la clase
        RateLimited annotation = resolverAnotacion(handlerMethod);
        if (annotation == null) {
            return true;
        }

        // Extrae scope y límites de la anotación
        String scope = annotation.scope();
        int maxRequests = annotation.maxRequests();
        long windowSeconds = annotation.windowSeconds();

        // Si la anotación no especifica valores, usar configuración de application.yml
        if (maxRequests <= 0 || windowSeconds <= 0) {
            RateLimitingProperties.Limit limit = properties.limitFor(scope);
            if (maxRequests <= 0) {
                maxRequests = limit.maxRequests();
            }
            if (windowSeconds <= 0) {
                windowSeconds = limit.windowSeconds();
            }
        }

        // Resuelve la clave de límite (email del usuario o IP)
        String key = resolverClave(request);

        // El check lanza TooManyRequestsException (429) si el límite se excede
        rateLimiterService.check(scope, key, maxRequests, windowSeconds);
        return true;
    }

    /**
     * Busca la anotación {@link RateLimited} en el método del handler.
     * Si no está en el método, busca en la clase del controlador (herencia
     * a nivel controlador).
     *
     * @param handlerMethod método del controlador resuelto por Spring MVC
     * @return la anotación {@link RateLimited} o {@code null} si no existe
     */
    private RateLimited resolverAnotacion(HandlerMethod handlerMethod) {
        RateLimited atMetodo = handlerMethod.getMethodAnnotation(RateLimited.class);
        if (atMetodo != null) {
            return atMetodo;
        }
        return handlerMethod.getBeanType().getAnnotation(RateLimited.class);
    }

    /**
     * Resuelve la clave de rate limiting: email del usuario autenticado
     * o, en su defecto, la IP del cliente.
     *
     * <p>Si el usuario está autenticado y su principal es un String (email),
     * se usa el email. Si no, se usa la IP del cliente (soporta encabezado
     * X-Forwarded-For para proxys).</p>
     *
     * @param request objeto {@link HttpServletRequest}
     * @return clave de rate limiting (email o IP, nunca {@code null})
     */
    private String resolverClave(HttpServletRequest request) {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof String email && !email.equals("anonymousUser")) {
            return email;
        }

        // Si no hay autenticación, usa la IP del cliente
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }
        // Si hay múltiples proxies en cadena, toma la primera (IP real del cliente)
        if (ip != null && ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(',')).trim();
        }
        return ip != null ? ip : "anon";
    }
}


