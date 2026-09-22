package com.tecmilenio.mapsconect.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuración del manejo de recursos estáticos.
 *
 * <p>Registra manejadores de recursos para servir contenido estático
 * (HTML, CSS, JS, imágenes, fuentes) desde:</p>
 * <ul>
 *   <li>{@code classpath:/static/} — recursos empaquetados en el JAR.</li>
 *   <li>{@code ./frontend/} (o {@code ../frontend/}) — desarrollo local.</li>
 *   <li>{@code System.getProperty("user.dir") + "/frontend"} — fallback
 *       si los paths relativos no resuelven.</li>
 * </ul>
 *
 * <p>El {@code cachePeriod = 0} fuerza al navegador a validar siempre la
 * copia local de los assets, lo cual es útil durante desarrollo. En
 * producción, los assets suelen estar versionados (hash en el nombre)
 * y pueden ser servidos con cache.</p>
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(StaticResourceConfig.class);

    /**
     * Registra las ubicaciones de recursos estáticos.
     *
     * <p>Intenta varias rutas relativas para encontrar la carpeta {@code frontend}
     * (dependiendo de cómo se invoque la aplicación), y siempre incluye
     * {@code classpath:/static/} como fuente base.</p>
     *
     * @param registry registro de manejadores de recursos de Spring MVC
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Siempre incluir el classpath de recursos estáticos del JAR
        List<String> locations = new ArrayList<>();
        locations.add("classpath:/static/");

        File cwd = new File(".").getAbsoluteFile();
        log.info("[StaticResourceConfig] working dir = {}", cwd);

        // Busca la carpeta frontend en varias rutas posibles
        for (String rel : new String[]{"frontend", "../frontend"}) {
            File dir = new File(cwd, rel);
            if (dir.isDirectory()) {
                locations.add(dir.toPath().toUri().toString());
                log.info("[StaticResourceConfig] frontend encontrado en {}", dir);
            }
        }

        // Fallback adicional usando user.dir del sistema
        if (locations.size() == 1) {
            File abs = new File(System.getProperty("user.dir"), "frontend");
            if (abs.isDirectory()) {
                locations.add(abs.toPath().toUri().toString());
                log.info("[StaticResourceConfig] frontend via user.dir en {}", abs);
            }
        }

        log.info("[StaticResourceConfig] locations = {}", locations);
        registry.addResourceHandler("/**")
                .addResourceLocations(locations.toArray(new String[0]))
                .setCachePeriod(0);
    }
}
