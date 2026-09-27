package com.tecmilenio.mapsconect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Clase principal de arranque de la aplicacion Spring Boot.
 *
 * <p>{@code @EnableScheduling} activa el soporte de tareas programadas
 * necesario para el job de limpieza de refresh tokens expirados.</p>
 */
@SpringBootApplication
@EnableScheduling
public class MapsConectApplication {

    public static void main(String[] args) {
        SpringApplication.run(MapsConectApplication.class, args);
    }

}

