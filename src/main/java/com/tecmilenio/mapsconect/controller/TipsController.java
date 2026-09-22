package com.tecmilenio.mapsconect.controller;

import com.tecmilenio.mapsconect.dto.ApiResponse;
import com.tecmilenio.mapsconect.dto.TipDTO;
import com.tecmilenio.mapsconect.dto.TipRequestDTO;
import com.tecmilenio.mapsconect.service.TipService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador de tips académicos.
 *
 * <p>Los tips son publicaciones breves de contenido académico (consejos,
 * trucos, enlaces útiles) asociadas a una materia. Los estudiantes y
 * profesores pueden crear tips, votarlos (sistema de upvotes) y retirar
 * su voto.</p>
 *
 * <p>El listado se personaliza según el rol y la carrera del usuario
 * autenticado.</p>
 */
@RestController
@RequestMapping("/tips")
public class TipsController {

    @Autowired
    private TipService tipService;

    /**
     * Lista los tips académicos visibles para el usuario autenticado.
     *
     * <p>Los tips se filtran según la carrera y rol del usuario para
     * mostrar contenido relevante.</p>
     *
     * @param authentication usuario autenticado
     * @return 200 OK con la lista de tips
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TipDTO>>> listar(Authentication authentication) {
        List<TipDTO> tips = tipService.listarPara(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(tips, "Tips obtenidos"));
    }

    /**
     * Crea un nuevo tip académico.
     *
     * @param request DTO con el contenido del tip y la materia asociada
     * @param authentication usuario que crea el tip
     * @return 201 Created con el tip creado
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TipDTO>> crear(
            @Valid @RequestBody TipRequestDTO request,
            Authentication authentication) {

        TipDTO tip = tipService.crear(authentication.getName(), request.getContenido(), request.getIdMateria());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(tip, "Tip publicado correctamente"));
    }

    /**
     * Registra un voto positivo (upvote) en un tip por parte del usuario.
     *
     * <p>No se permiten votos duplicados; votar nuevamente retira el voto.</p>
     *
     * @param id identificador del tip
     * @param authentication usuario que vota
     * @return 200 OK con el tip actualizado (incluye el nuevo conteo de votos)
     */
    @PostMapping("/{id}/voto")
    public ResponseEntity<ApiResponse<TipDTO>> votar(@PathVariable Integer id, Authentication authentication) {
        TipDTO tip = tipService.votar(authentication.getName(), id);
        return ResponseEntity.ok(ApiResponse.success(tip, "Voto registrado"));
    }

    /**
     * Retira el voto positivo que el usuario haya emitido en un tip.
     *
     * @param id identificador del tip
     * @param authentication usuario que retira su voto
     * @return 200 OK con confirmación de voto eliminado
     */
    @DeleteMapping("/{id}/voto")
    public ResponseEntity<ApiResponse<Void>> desvotar(@PathVariable Integer id, Authentication authentication) {
        tipService.desvotar(authentication.getName(), id);
        return ResponseEntity.ok(ApiResponse.success(null, "Voto eliminado"));
    }

}


