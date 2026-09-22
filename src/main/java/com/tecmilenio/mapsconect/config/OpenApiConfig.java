package com.tecmilenio.mapsconect.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración OpenAPI 3.0 con documentación automática de endpoints
 * y esquema de seguridad JWT (Bearer token).
 *
 * <p>Swagger UI disponible en:</p>
 * <ul>
 *   <li>http://localhost:8080/api/swagger-ui.html</li>
 *   <li>Especificación JSON: http://localhost:8080/api/v3/api-docs</li>
 * </ul>
 *
 * <p>La configuración define:</p>
 * <ul>
 *   <li>Esquema de seguridad Bearer Authentication (JWT por header Authorization).</li>
 *   <li>Metadata de la API (título, descripción, versión, contacto, licencia).</li>
 *   <li>Organización por tags (auto-generados por anotaciones @Tag).</li>
 * </ul>
 *
 * <p>Los endpoints protegidos pueden probarse directamente desde Swagger UI
 * haciendo clic en el botón "Authorize" e ingresando el token JWT.</p>
 */
@Configuration
public class OpenApiConfig {

    /**
     * Construye el objeto {@link OpenAPI} con metadata y esquema de seguridad JWT.
     *
     * <p>El esquema de seguridad "Bearer Authentication" se configura como
     * HTTP Bearer con formato JWT, y se aplica globalmente a todos los
     * endpoints mediante {@link SecurityRequirement}.</p>
     *
     * @return configuración OpenAPI personalizada
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .name("Authorization")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Proporcione el token JWT previamente obtenido en /auth/login o /auth/registrar")))
                .info(new Info()
                        .title("MAPS Connect API")
                        .description("API REST para la plataforma académica y de mentoría MAPS. " +
                                "Incluye endpoints de autenticación, gestión de usuarios, " +
                                "foro, mensajería, recursos académicos y notificaciones.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo MAPS")
                                .email("desarrollo@mapsconect.com"))
                        .license(new License().name("Propietario").url("https://mapsconect.com")));
    }

    /**
     * Personaliza la documentación OpenAPI después de que SpringDoc
     * escanea los controladores.
     *
     * <p>Actualmente itera sobre los tags generados para permitir
     * personalizaciones futuras (descripciones, orden, etc.).</p>
     *
     * @return customizer que opera sobre el modelo OpenAPI tras el escaneo
     */
    @Bean
    public OpenApiCustomizer customOpenApiCustomizer() {
        return openApi -> openApi.getTags().forEach(tag -> {
            // Los tags ya son auto-generados por paquete; aquí podríamos
            // personalizar descripciones si fuera necesario.
        });
    }
}


