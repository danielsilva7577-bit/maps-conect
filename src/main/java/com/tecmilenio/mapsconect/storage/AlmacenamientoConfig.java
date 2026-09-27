package com.tecmilenio.mapsconect.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuración de la capa de almacenamiento.
 *
 * <p>Selecciona el bean primario según la propiedad {@code app.storage.type}:</p>
 * <ul>
 *   <li>{@code local} (default): usa el disco del servidor.</li>
 *   <li>{@code s3}: usa almacenamiento de objetos en la nube (AWS S3, MinIO, Cloudflare R2).</li>
 * </ul>
 */
@Configuration
public class AlmacenamientoConfig {

    @Bean
    @Primary
    public AlmacenamientoService almacenamientoPrincipal(
            @Value("${app.storage.type:local}") String tipo,
            DiscoLocalAlmacenamientoService discoLocal,
            S3AlmacenamientoService s3Service) {

        if ("s3".equalsIgnoreCase(tipo)) {
            return s3Service;
        }
        return discoLocal;
    }

}
