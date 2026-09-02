package com.tecmilenio.mapsconect.controller;

import com.tecmilenio.mapsconect.dto.ApiResponse;
import com.tecmilenio.mapsconect.dto.EstudianteDTO;
import com.tecmilenio.mapsconect.dto.EstudianteRequestDTO;
import com.tecmilenio.mapsconect.service.EstudianteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/estudiantes")
public class EstudianteController {

    @Autowired
    private EstudianteService estudianteService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<EstudianteDTO>>> listar() {
        return ResponseEntity.ok(ApiResponse.success(estudianteService.listar()));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<EstudianteDTO>> obtenerMiPerfil(Authentication authentication) {
        EstudianteDTO estudiante = estudianteService.obtenerPerfilActual(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(estudiante));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EstudianteDTO>> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(estudianteService.obtenerPorId(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EstudianteDTO>> crear(
            Authentication authentication,
            @Valid @RequestBody EstudianteRequestDTO dto) {
        EstudianteDTO estudiante = estudianteService.crearPerfil(authentication.getName(), dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(estudiante, "Perfil de estudiante creado correctamente"));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<EstudianteDTO>> actualizar(
            Authentication authentication,
            @Valid @RequestBody EstudianteRequestDTO dto) {
        EstudianteDTO estudiante = estudianteService.actualizarPerfil(authentication.getName(), dto);
        return ResponseEntity.ok(ApiResponse.success(estudiante, "Perfil actualizado correctamente"));
    }

}

