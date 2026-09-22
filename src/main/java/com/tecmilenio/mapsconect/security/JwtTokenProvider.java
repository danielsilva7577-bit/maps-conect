package com.tecmilenio.mapsconect.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Proveedor de tokens JWT para la autenticación de la plataforma MAPS Connect.
 *
 * <p>Responsabilidades:</p>
 * <ul>
 *   <li>Generar tokens JWT firmados con HMAC-SHA512.</li>
 *   <li>Validar tokens (firma y expiración).</li>
 *   <li>Extraer el nombre de usuario (subject) de un token válido.</li>
 * </ul>
 *
 * <p>La clave secreta se obtiene de la variable de entorno {@code JWT_SECRET}.
 * Si no está definida, se usa una clave de desarrollo embebida como fallback,
 * pero la aplicación se rehúsa a arrancar en producción sin una clave propia.</p>
 *
 * <p>Ejemplo de uso:</p>
 * <pre>{@code
 * String token = jwtTokenProvider.generateToken("usuario@ejemplo.com");
 * String username = jwtTokenProvider.getUsernameFromToken(token);
 * boolean valido = jwtTokenProvider.validateToken(token);
 * }</pre>
 */
@Component
public class JwtTokenProvider {

    /**
     * Clave embebida de respaldo, SOLO para desarrollo local.
     * La aplicación requiere {@code JWT_SECRET} (mín. 64 caracteres) en producción.
     * Esta constante existe para evitar que la app falle al arrancar en modo dev
     * cuando no se ha configurado la variable de entorno.
     */
    private static final String SECRET_DEV_FALLBACK =
            "mapsconnect-tecmilenio-super-secret-key-2024-desarrollo-local-minimo-64-caracteres-seguro";

    /** Clave secreta cargada desde la variable de entorno JWT_SECRET. */
    @Value("${JWT_SECRET:}")
    private String jwtSecret;

    /** Tiempo de expiración del token en milisegundos (configurado en application.yml). */
    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    /** Environment de Spring, usado para detectar el perfil activo. */
    private final Environment environment;

    public JwtTokenProvider(Environment environment) {
        this.environment = environment;
    }

    /**
     * Inicializa la clave de firma tras inyectar las dependencias.
     *
     * <p>Si {@code JWT_SECRET} no está configurada (o coincide con el fallback de dev):
     * <ul>
     *   <li>En perfiles {@code dev}/{@code default}: usa la clave de fallback.</li>
     *   <li>En cualquier otro perfil: lanza {@link IllegalStateException} para
     *       forzar la configuración explícita de seguridad en producción.</li>
     * </ul>
     */
    @PostConstruct
    void prepararFirma() {
        byte[] bytes = jwtSecret == null ? new byte[0] : jwtSecret.getBytes();
        boolean esFallbackDev = jwtSecret == null || jwtSecret.isBlank()
                || SECRET_DEV_FALLBACK.equals(jwtSecret);
        if (esFallbackDev) {
            // En desarrollo la clave por defecto es tolerable; en producción la app debe
            // arrancar con JWT_SECRET propio o se rehúsa a operar.
            if (!isActivoPerfil("dev") && !isActivoPerfil("default")) {
                throw new IllegalStateException(
                        "JWT_SECRET no configurado. Define la variable de entorno JWT_SECRET (mín. 64 caracteres).");
            }
            jwtSecret = SECRET_DEV_FALLBACK;
        }
    }

    /**
     * Verifica si el perfil dado está activo, revisando tanto el
     * {@link Environment} de Spring como las propiedades del sistema
     * y las variables de entorno.
     *
     * @param perfil nombre del perfil a verificar (ej. "dev", "prod")
     * @return {@code true} si el perfil está activo en alguna fuente
     */
    private boolean isActivoPerfil(String perfil) {
        if (perfil == null) {
            return false;
        }
        if (environment != null && environment.acceptsProfiles(org.springframework.core.env.Profiles.of(perfil))) {
            return true;
        }
        String activos = System.getProperty("spring.profiles.active", "");
        String env = System.getenv("SPRING_PROFILES_ACTIVE");
        String combinado = (activos + " " + (env == null ? "" : env)).toLowerCase();
        return combinado.contains(perfil);
    }

    /**
     * Construye la {@link SecretKey} HMAC a partir del secret configurado.
     *
     * @return clave secreta para firmar/verificar tokens JWT
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * @return tiempo de expiración en segundos (convertido desde milisegundos).
     */
    public long getExpiresInSeconds() {
        return jwtExpirationMs / 1000L;
    }

    /**
     * Genera un token JWT sin claims adicionales.
     *
     * @param username email o nombre de usuario que será el subject del token
     * @return token JWT compactado
     */
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    /**
     * Genera un token JWT incluyendo claims personalizados.
     *
     * @param username email o nombre de usuario
     * @param claims   mapa de claims adicionales a incluir en el token
     * @return token JWT compactado
     */
    public String generateTokenWithClaims(String username, Map<String, Object> claims) {
        return createToken(claims, username);
    }

    /**
     * Construye y firma un token JWT con los claims, subject, timestamps y firma dados.
     *
     * @param claims   claims a incluir
     * @param subject  subject del token (normalmente el email del usuario)
     * @return token JWT firmado con HMAC-SHA512
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Extrae el subject (username/email) de un token JWT sin validar la expiración.
     * La validación completa se realiza con {@link #validateToken(String)}.
     *
     * @param token token JWT
     * @return username extraído del subject
     */
    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    /**
     * Valida que el token esté firmado correctamente y no esté expirado.
     *
     * @param token token JWT a validar
     * @return {@code true} si el token es válido, {@code false} en caso contrario
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            // Cualquier error de firma, expiración o formato invalida el token
            return false;
        }
    }

    /**
     * Extrae todos los claims del token (subject, expiración, claims personalizados).
     * Asume que el token ya fue validado.
     *
     * @param token token JWT
     * @return objeto {@link Claims} con todos los datos del token
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}


