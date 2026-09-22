package com.tecmilenio.mapsconect.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.tecmilenio.mapsconect.security.JwtAuthenticationFilter;

/**
 * Configuración de seguridad de la aplicación.
 *
 * <p>Define la cadena de filtros de Spring Security con política de tokens
 * ({@code STATELESS}), CSRF deshabilitado (la API es consumida por un
 * frontend separado y los tokens se envían por header), y cabeceras de
 * seguridad recomendadas (HSTS, CSP, frame-deny, anti-cache).</p>
 *
 * <p>El filtro JWT ({@link JwtAuthenticationFilter}) se inserta antes de
 * {@code UsernamePasswordAuthenticationFilter} para interceptar y validar
 * el token Bearer antes de que Spring Security procese la autenticación.</p>
 *
 * <p>Rutas públicas (permitAll):</p>
 * <ul>
 *   <li>{@code /auth/login}, {@code /auth/registrar} — registro y login.</li>
 *   <li>{@code /health} — health check.</li>
 *   <li>Recursos estáticos del frontend ({@code /css/**}, {@code /js/**}, etc.).</li>
 * </ul>
 * Todo lo demás requiere autenticación.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Registra el filtro JWT como bean para poder inyectarlo en el
     * {@link SecurityFilterChain}.
     *
     * @return instancia de {@link JwtAuthenticationFilter}
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    /**
     * Codificador de contraseñas basado en BCrypt.
     *
     * <p>BCrypt es el estándar recomendado por Spring Security porque
     * incluye salt y es resistente a ataques de fuerza bruta mediante
     * un factor de coste (load factor).</p>
     *
     * @return instancia de {@link BCryptPasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Expone el {@link AuthenticationManager} para que los controladores
     * de autenticación puedan autenticar usuarios manualmente (ej. en login).
     *
     * @param config configuración de autenticación proporcionada por Spring
     * @return {@link AuthenticationManager} global
     * @throws Exception si no se puede construir el AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Define la cadena de filtros de seguridad HTTP.
     *
     * <p>Configuración:</p>
     * <ul>
     *   <li><b>CSRF</b>: deshabilitado (API stateless, tokens manejados en header).</li>
     *   <li><b>Sesiones</b>: STATELESS (sin estado server-side).</li>
     *   <li><b>Headers de seguridad</b>: HSTS 1 año, CSP restrictiva, frame deny,
     *       anti-cache en respuestas dinámicas, cache breve en assets.</li>
     *   <li><b>Autorizaciones</b>: rutas públicas explícitas, todo lo demás requiere auth.</li>
     *   <li><b>Filtro JWT</b>: insertado antes del filtro de usuario/contraseña.</li>
     * </ul>
     *
     * @param http builder de configuración de seguridad HTTP
     * @return la cadena de filtros construida
     * @throws Exception si falla la configuración
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF no se usa en APIs stateless con tokens
            .csrf(csrf -> csrf.disable())

            // Sin estado: cada request lleva su propio token
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(errors -> errors
                .authenticationEntryPoint((request, response, error) -> response.sendError(401))
                .accessDeniedHandler((request, response, error) -> response.sendError(403)))

            // Cabeceras de seguridad recomendadas por OWASP
            .headers(headers -> headers
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000)) // 1 año
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; font-src 'self' https://fonts.gstatic.com; img-src 'self' data:; connect-src 'self'; frame-ancestors 'none'; base-uri 'self'; form-action 'self'"))
                .frameOptions(frame -> frame.deny()) // Bloquear clickjacking
                .referrerPolicy(referrer -> referrer.policy(
                        org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                // Cabeceras personalizadas: anti-cache en contenido dinámico, cache breve en assets
                .addHeaderWriter(new org.springframework.security.web.header.HeaderWriter() {
                    @Override
                    public void writeHeaders(jakarta.servlet.http.HttpServletRequest request,
                                             jakarta.servlet.http.HttpServletResponse response) {
                        String uri = request.getRequestURI();
                        boolean esRecursoEstatico = uri.startsWith("/css/")
                                || uri.startsWith("/js/")
                                || uri.startsWith("/img/")
                                || uri.startsWith("/assets/")
                                || uri.startsWith("/favicon")
                                || uri.startsWith("/robots.txt")
                                || uri.equals("/sw.js");
                        response.setHeader("X-Permitted-Cross-Domain-Policies", "none");
                        if (!esRecursoEstatico) {
                            // No cachear respuestas dinámicas ni dejar que el navegador
                            // almacene tokens/sesiones.
                            response.setHeader("Cache-Control", "no-store, max-age=0, must-revalidate");
                            response.setHeader("Pragma", "no-cache");
                            response.setHeader("Expires", "0");
                        } else {
                            // Assets versionados: permitir cache breve para rendimiento
                            response.setHeader("Cache-Control", "public, max-age=3600");
                        }
                    }
                }))

            // Definición de rutas públicas vs autenticadas
            .authorizeHttpRequests(authz -> authz
                // Rutas de autenticación: acceso público (no requieren token)
                .requestMatchers("/auth/login", "/auth/registrar").permitAll()
                // Health check: acceso público
                .requestMatchers("/health").permitAll()
                // Recursos estáticos del frontend: acceso público
                .requestMatchers(
                    "/", "/index.html", "/login.html", "/registro.html", "/onboarding.html",
                    "/pages/**", "/css/**", "/js/**", "/assets/**",
                    "/favicon.ico", "/robots.txt", "/sw.js").permitAll()
                // Todo lo demás requiere autenticación válida
                .anyRequest().authenticated()
            )

            // Inserta el filtro JWT antes del filtro estándar de autenticación
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}


