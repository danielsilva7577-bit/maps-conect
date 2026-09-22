package com.tecmilenio.mapsconect.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filtro de autenticación JWT que intercepta cada request HTTP.
 *
 * <p>Extiende {@link OncePerRequestFilter} para garantizar que el filtro
 * se ejecute exactamente una vez por request, incluso en nodos de dispatch
 * asíncronos o de error.</p>
 *
 * <p>Lógica del filtro:</p>
 * <ol>
 *   <li>Extrae el token JWT del header {@code Authorization: Bearer ...}.</li>
 *   <li>En rutas SSE ({@code /mensajes/{id}/stream}), también admite el token
 *       como query param {@code ?token=...}, ya que {@code EventSource} en el
 *       frontend no permite cabeceras personalizadas.</li>
 *   <li>Si el token es válido, carga el usuario desde la BD y construye
 *       un {@link UsernamePasswordAuthenticationToken} que se inyecta en el
 *       {@link SecurityContextHolder}, dejando a los controladores y al
 *       {@code Authentication} argument resolver disponible la identidad.</li>
 * </ol>
 *
 * <p>Si el token es inválido o no está presente, el filtro simplemente
 * continúa la cadena y deja que la seguridad de Spring gestione el
 * rechazo (normalmente un 401).</p>
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** Proveedor de tokens JWT (firma/validación). */
    @Autowired
    private JwtTokenProvider tokenProvider;

    /** Servicio de carga de usuarios (consulta la BD por el email). */
    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * Procesa cada request: extrae el JWT, valida la firma y, si es válido,
     * establece la autenticación en el {@link SecurityContextHolder}.
     *
     * @param request   objeto {@link HttpServletRequest} con el token
     * @param response  objeto {@link HttpServletResponse}
     * @param filterChain cadena de filtros a continuar
     * @throws ServletException en caso de error de servlet
     * @throws IOException      en caso de error de I/O
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                String username = tokenProvider.getUsernameFromToken(jwt);

                // Carga los detalles completos del usuario (incluye authorities)
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                new org.springframework.security.authentication.AccountStatusUserDetailsChecker().check(userDetails);

                // Construye la autenticación con los authorities del usuario
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Inyecta la autenticación en el contexto de seguridad para este request
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            // Loggea el error pero no interrumpe la cadena: deja que Spring Security
            // gestione la denegación (return 401) a través de sus filtros posteriores.
            logger.error("Fallo al autenticar con JWT", ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Determina si este filtro debe ignorarse para el request dado.
     *
     * <p>Siempre devuelve {@code false}: el filtro se ejecuta en todas las rutas.
     * El comentario explica que la extracción por query param de token
     * se aplica únicamente a rutas SSE (ver {@link #getJwtFromRequest}).</p>
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Solo aplicar la lógica de token por query param a subidas vía SSE.
        // El resto de rutas deben usar la cabecera Authorization para evitar
        // que los JWT queden expuestos en los logs de acceso (query params).
        return false;
    }

    /**
     * Extrae el token JWT del request.
     *
     * <p>Primero busca en el header {@code Authorization: Bearer ...}. Si no lo
     * encuentra y la ruta es SSE ({@code /mensajes/{id}/stream}), busca el
     * token como query param {@code ?token=...}, ya que EventSource del
     * frontend no permite cabeceras personalizadas.</p>
     *
     * @param request objeto {@link HttpServletRequest}
     * @return el token JWT sin el prefijo "Bearer ", o {@code null} si no se encuentra
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        // Server-Sent Events (EventSource) no permite cabeceras personalizadas:
        // el token se transporta como query param "token" SOLO en rutas SSE.
        // En cualquier otra ruta el token por query param se ignora (evita fugas en logs).
        if (esRutaSse(request)) {
            String paramToken = request.getParameter("token");
            if (StringUtils.hasText(paramToken)) {
                return paramToken;
            }
        }
        return null;
    }

    /**
     * Determina si la request corresponde a un endpoint de streaming SSE.
     * Las rutas SSE terminan con {@code /stream}.
     *
     * @param request objeto {@link HttpServletRequest}
     * @return {@code true} si es una ruta SSE, {@code false} en caso contrario
     */
    private boolean esRutaSse(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri != null && uri.endsWith("/stream");
    }

}
