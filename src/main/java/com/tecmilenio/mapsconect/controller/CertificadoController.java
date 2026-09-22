package com.tecmilenio.mapsconect.controller;

import com.tecmilenio.mapsconect.dto.ApiResponse;
import com.tecmilenio.mapsconect.dto.AsignacionCertificadoDTO;
import com.tecmilenio.mapsconect.dto.CertificadoDTO;
import com.tecmilenio.mapsconect.dto.CertificadoRequestDTO;
import com.tecmilenio.mapsconect.service.CertificadoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador de certificados académicos MAPS Connect.
 *
 * <p>Los certificados son trayectorias de aprendizaje opcionales que agrupan
 * materias de distintos semestres. Los estudiantes pueden asignarlos a su
 * ruta académica para seguirlos durante su carrera.</p>
 *
 * <p>Expone endpoints para listar, obtener, crear certificados y vincular
 * materias a ellos, así como asignar certificados a estudiantes.</p>
 */
@RestController
@RequestMapping("/certificados")
public class CertificadoController {

    @Autowired
    private CertificadoService certificadoService;

    /**
     * Lista todos los certificados disponibles.
     *
     * @return 200 OK con la lista de certificados
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CertificadoDTO>>> listar() {
        List<CertificadoDTO> certificados = certificadoService.listar();
        return ResponseEntity.ok(ApiResponse.success(certificados, "Certificados obtenidos"));
    }

    /**
     * Obtiene un certificado por su ID.
     *
     * @param id identificador del certificado
     * @return 200 OK con el certificado solicitado
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CertificadoDTO>> obtener(@PathVariable Integer id) {
        CertificadoDTO certificado = certificadoService.obtener(id);
        return ResponseEntity.ok(ApiResponse.success(certificado, "Certificado obtenido"));
    }

    /**
     * Crea un nuevo certificado con nombre y descripción.
     *
     * @param request datos del certificado a crear
     * @return 201 Created con el certificado creado
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CertificadoDTO>> crear(
            @Valid @RequestBody CertificadoRequestDTO request) {

        CertificadoDTO certificado = certificadoService.crear(
                request.getNombre(), request.getDescripcion());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(certificado, "Certificado creado correctamente"));
    }

    /**
     * Vincula una materia a un certificado existente.
     *
     * @param idCertificado ID del certificado
     * @param idMateria     ID de la materia a vincular
     * @return 200 OK con el certificado actualizado
     */
    @PostMapping("/{idCertificado}/materias/{idMateria}")
    public ResponseEntity<ApiResponse<CertificadoDTO>> vincularMateria(
            @PathVariable Integer idCertificado,
            @PathVariable Integer idMateria) {

        CertificadoDTO certificado = certificadoService.vincularMateria(idCertificado, idMateria);
        return ResponseEntity.ok(ApiResponse.success(certificado, "Materia vinculada al certificado"));
    }

    /**
     * Asigna un certificado a un estudiante.
     *
     * @param request DTO con el ID del certificado y del estudiante
     * @return 200 OK con el certificado y su estado de asignación
     */
    @PostMapping("/asignar")
    public ResponseEntity<ApiResponse<CertificadoDTO>> asignarAEstudiante(
            @Valid @RequestBody AsignacionCertificadoDTO request) {

        CertificadoDTO certificado = certificadoService.asignarAEstudiante(request);
        return ResponseEntity.ok(ApiResponse.success(certificado, "Certificado asignado al estudiante"));
    }

    /**
     * Obtiene la ruta de certificados de un estudiante específico.
     *
     * @param idEstudiante ID del estudiante
     * @return 200 OK con la lista de certificados asignados al estudiante
     */
    @GetMapping("/estudiantes/{idEstudiante}/ruta")
    public ResponseEntity<ApiResponse<List<CertificadoDTO>>> rutaEstudiante(
            @PathVariable Integer idEstudiante) {

        List<CertificadoDTO> certificados = certificadoService.listarDeEstudiante(idEstudiante);
        return ResponseEntity.ok(ApiResponse.success(certificados, "Ruta del estudiante obtenida"));
    }

}


