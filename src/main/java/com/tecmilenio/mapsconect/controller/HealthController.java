package com.tecmilenio.mapsconect.controller;

import com.tecmilenio.mapsconect.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador de salud (health check).
 *
 * <p>Endpoint público que permite verificar que el backend está activo y
 * respondiendo. Útil para load balancers, monitores externos y health checks
 * de infraestructura (Kubernetes, Docker, etc.).</p>
 *
 * <p>No requiere autenticación.</p>
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    /**
     * Verifica que el servicio está en funcionamiento.
     *
     * @return 200 OK con un mensaje de confirmación
     */
    @GetMapping
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
            ApiResponse.success("MAPS Connect Backend is running", "OK")
        );
    }

}


