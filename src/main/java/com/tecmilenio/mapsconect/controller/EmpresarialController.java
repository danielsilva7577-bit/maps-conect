package com.tecmilenio.mapsconect.controller;

import com.tecmilenio.mapsconect.dto.ApiResponse;
import com.tecmilenio.mapsconect.dto.EmpresaDTO;
import com.tecmilenio.mapsconect.dto.ResenaDTO;
import com.tecmilenio.mapsconect.dto.ResenaRequestDTO;
import com.tecmilenio.mapsconect.service.EmpresarialService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/empresarial")
public class EmpresarialController {

    @Autowired
    private EmpresarialService empresarialService;

    // Contrato consumido por frontend/js/pages/empresarial.js: { empresas: [...] }
    @GetMapping
    public ResponseEntity<Map<String, Object>> listar(
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) Double calificacionMinima) {
        List<EmpresaDTO> empresas = empresarialService.listarEmpresas(busqueda, calificacionMinima);
        return ResponseEntity.ok(Map.of("empresas", empresas));
    }

    @GetMapping("/{id}/resenas")
    public ResponseEntity<ApiResponse<List<ResenaDTO>>> listarResenas(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(empresarialService.listarResenasPorEmpresa(id)));
    }

    @PostMapping("/resenas")
    public ResponseEntity<ApiResponse<ResenaDTO>> crearResena(
            Authentication authentication,
            @Valid @RequestBody ResenaRequestDTO dto) {
        ResenaDTO resena = empresarialService.crearResena(authentication.getName(), dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(resena, "Reseña publicada correctamente"));
    }

}

