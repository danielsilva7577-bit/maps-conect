package com.tecmilenio.mapsconect.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.cors.origins:}")
    private String corsOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> origins = parseOrigins(corsOrigins);
        if (origins.isEmpty()) {
            origins = Arrays.asList(
                    "http://localhost:5500",
                    "http://localhost:3000",
                    "http://localhost:5173",
                    "http://localhost:63342"
            );
        }

        String[] originArray = origins.toArray(String[]::new);

        registry.addMapping("/**")
                .allowedOriginPatterns(originArray)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    private List<String> parseOrigins(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        for (String part : raw.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

}
