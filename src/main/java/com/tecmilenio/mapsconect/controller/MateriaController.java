package com.tecmilenio.mapsconect.controller;

import com.tecmilenio.mapsconect.dto.ApiResponse;
import com.tecmilenio.mapsconect.dto.MateriaDTO;
import com.tecmilenio.mapsconect.service.MateriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/materias")
public class MateriaController {

    @Autowired
    private MateriaService materiaService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MateriaDTO>>> listar(
            @RequestParam(required = false) String tipo) {
        List<MateriaDTO> materias = materiaService.listar(tipo);
        return ResponseEntity.ok(ApiResponse.success(materias));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MateriaDTO>> obtenerPorId(@PathVariable Integer id) {
        MateriaDTO materia = materiaService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.success(materia));
    }

}

