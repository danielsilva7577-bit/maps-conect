package com.tecmilenio.mapsconect.controller;

import com.tecmilenio.mapsconect.dto.ApiResponse;
import com.tecmilenio.mapsconect.dto.ResenaEmpresarialDTO;
import com.tecmilenio.mapsconect.entity.EmpresaVinculada;
import com.tecmilenio.mapsconect.entity.Estudiante;
import com.tecmilenio.mapsconect.entity.ResenaEmpresarial;
import com.tecmilenio.mapsconect.entity.Usuario;
import com.tecmilenio.mapsconect.exception.ResourceNotFoundException;
import com.tecmilenio.mapsconect.repository.EmpresaVinculadaRepository;
import com.tecmilenio.mapsconect.repository.EstudianteRepository;
import com.tecmilenio.mapsconect.repository.ResenaEmpresarialRepository;
import com.tecmilenio.mapsconect.repository.UsuarioRepository;
import com.tecmilenio.mapsconect.service.CarreraContextoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Controlador de la sección empresarial (Semestre Empresarial).
 *
 * <p>Permite listar empresas vinculadas (con filtros de búsqueda, calificación
 * y carrera), consultar reseñas de experiencias previas, y crear/editar/eliminar
 * reseñas como estudiante que realizó el Semestre Empresarial.</p>
 *
 * <p>Las reseñas solo pueden crearlas estudiantes de semestre 6 o superior
 * que hayan realizado el Semestre Empresarial en la empresa.</p>
 *
 * @see com.tecmilenio.mapsconect.entity.EmpresaVinculada
 * @see com.tecmilenio.mapsconect.entity.ResenaEmpresarial
 */
@RestController
@RequestMapping("/empresarial")
public class EmpresarialController {

    @Autowired
    private EmpresaVinculadaRepository empresaRepository;

    @Autowired
    private ResenaEmpresarialRepository resenaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private CarreraContextoService carreraContextoService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> listar(
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String modalidad,
            @RequestParam(required = false) Double calificacionMinima,
            Authentication authentication) {

        String carreraFiltrada = carreraContextoService.carreraDelUsuario(
                authentication == null ? null : authentication.getName());

        List<Object[]> filas = (busqueda != null && !busqueda.isBlank())
                ? empresaRepository.findEmpresasConFiltro(busqueda)
                : empresaRepository.findEmpresasConPromedio();

        List<Map<String, Object>> empresas = new ArrayList<>();
        for (Object[] fila : filas) {
            Map<String, Object> empresa = mapear(fila);
            if (calificacionMinima != null) {
                Double cal = (Double) empresa.get("calificacion");
                if (cal == null || cal < calificacionMinima) continue;
            }
            empresas.add(empresa);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        if (carreraFiltrada != null && !carreraFiltrada.isBlank()) {
            data.put("carreraFiltrada", carreraFiltrada);
        }
        data.put("empresas", empresas);
        return ResponseEntity.ok(ApiResponse.success(data, "Empresas obtenidas"));
    }

    /**
     * Resuelve el nombre de la carrera del estudiante autenticado.
     * Devuelve null para profesores/administradores, usuarios sin sesión
     * o estudiantes sin carrera registrada (en ese caso no se filtra).
     */

    @GetMapping("/{id}/resenas")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listarResenas(
            @PathVariable Integer id, Authentication authentication) {

        empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));

        Integer idUsuario = null;
        if (authentication != null && authentication.getName() != null) {
            Usuario usuario = usuarioRepository.findByEmail(authentication.getName()).orElse(null);
            if (usuario != null) {
                idUsuario = usuario.getId();
            }
        }

        int idFinal = idUsuario == null ? 0 : idUsuario;
        List<Map<String, Object>> resenas = new ArrayList<>();
        for (Object[] fila : resenaRepository.listarConAutor(id)) {
            Map<String, Object> resena = new LinkedHashMap<>();
            resena.put("id", ((Number) fila[0]).intValue());
            resena.put("calificacion", ((Number) fila[1]).intValue());
            resena.put("proyectoDesarrollado", fila[2]);
            resena.put("aprendizajes", fila[3]);
            resena.put("recomendaciones", fila[4]);
            resena.put("fecha", fila[5] == null ? null : fila[5].toString());
            resena.put("autor", fila[6]);
            int autorId = ((Number) fila[7]).intValue();
            resena.put("autorId", autorId);
            resena.put("semestre", fila[8] == null ? null : ((Number) fila[8]).intValue());
            resena.put("miResena", idFinal > 0 && autorId == idFinal);
            resenas.add(resena);
        }

        return ResponseEntity.ok(ApiResponse.success(resenas, "Experiencias obtenidas"));
    }

    @PostMapping("/{id}/resenas")
    public ResponseEntity<ApiResponse<Map<String, Object>>> crearResena(
            @PathVariable Integer id,
            @Valid @RequestBody ResenaEmpresarialDTO request,
            Authentication authentication) {

        EmpresaVinculada empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));

        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Estudiante estudiante = estudianteRepository.findByIdUsuario(usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Completa tu perfil de estudiante antes de compartir tu experiencia"));

        if (estudiante.getSemestreActual() == null || estudiante.getSemestreActual() < 6) {
            throw new IllegalArgumentException(
                    "Solo estudiantes de semestre 6 o superior pueden reseñar el Semestre Empresarial");
        }

        if (resenaRepository.existsByIdEstudianteAndIdEmpresa(estudiante.getId(), id)) {
            throw new IllegalArgumentException("Ya compartiste tu experiencia en esta empresa");
        }

        ResenaEmpresarial resena = resenaRepository.save(ResenaEmpresarial.builder()
                .idEstudiante(estudiante.getId())
                .idEmpresa(empresa.getId())
                .calificacion(request.getCalificacion())
                .proyectoDesarrollado(request.getProyectoDesarrollado())
                .aprendizajes(request.getAprendizajes())
                .recomendaciones(request.getRecomendaciones())
                .fechaResena(LocalDateTime.now())
                .build());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", resena.getId());
        data.put("calificacion", resena.getCalificacion());
        data.put("proyectoDesarrollado", resena.getProyectoDesarrollado());
        data.put("aprendizajes", resena.getAprendizajes());
        data.put("recomendaciones", resena.getRecomendaciones());
        data.put("fecha", resena.getFechaResena().toString());
        data.put("autor", usuario.getNombreCompleto());
        data.put("autorId", usuario.getId());
        data.put("miResena", true);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(data, "Experiencia compartida correctamente"));
    }

    @PutMapping("/{id}/resenas")
    public ResponseEntity<ApiResponse<Map<String, Object>>> editarResena(
            @PathVariable Integer id,
            @Valid @RequestBody ResenaEmpresarialDTO request,
            Authentication authentication) {

        empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));

        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Estudiante estudiante = estudianteRepository.findByIdUsuario(usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Completa tu perfil de estudiante antes de compartir tu experiencia"));

        ResenaEmpresarial resena = resenaRepository
                .findFirstByIdEstudianteAndIdEmpresa(estudiante.getId(), id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No has compartido una experiencia en esta empresa todavía"));

        resena.setCalificacion(request.getCalificacion());
        resena.setProyectoDesarrollado(request.getProyectoDesarrollado());
        resena.setAprendizajes(request.getAprendizajes());
        resena.setRecomendaciones(request.getRecomendaciones());
        resenaRepository.save(resena);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", resena.getId());
        data.put("calificacion", resena.getCalificacion());
        data.put("proyectoDesarrollado", resena.getProyectoDesarrollado());
        data.put("aprendizajes", resena.getAprendizajes());
        data.put("recomendaciones", resena.getRecomendaciones());
        data.put("fecha", resena.getFechaResena().toString());
        data.put("autor", usuario.getNombreCompleto());
        data.put("autorId", usuario.getId());
        data.put("miResena", true);

        return ResponseEntity.ok(ApiResponse.success(data, "Experiencia actualizada correctamente"));
    }

    @DeleteMapping("/{id}/resenas")
    public ResponseEntity<ApiResponse<Map<String, Object>>> eliminarResena(
            @PathVariable Integer id, Authentication authentication) {

        empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));

        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Estudiante estudiante = estudianteRepository.findByIdUsuario(usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Completa tu perfil de estudiante antes de compartir tu experiencia"));

        ResenaEmpresarial resena = resenaRepository
                .findFirstByIdEstudianteAndIdEmpresa(estudiante.getId(), id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No has compartido una experiencia en esta empresa todavía"));

        resenaRepository.delete(resena);

        return ResponseEntity.ok(ApiResponse.success(Map.of(), "Experiencia eliminada correctamente"));
    }

    private Map<String, Object> mapear(Object[] fila) {
        Integer id = ((Number) fila[0]).intValue();
        String nombre = (String) fila[1];
        String sector = (String) fila[2];
        String sitioWeb = (String) fila[3];
        String descripcion = (String) fila[4];
        String carrerasAfines = (String) fila[5];
        Double calificacion = fila[6] == null ? null : ((Number) fila[6]).doubleValue();
        long totalResenas = fila[7] == null ? 0 : ((Number) fila[7]).longValue();

        Map<String, Object> empresa = new HashMap<>();
        empresa.put("id", id);
        empresa.put("nombre", nombre);
        empresa.put("ciudad", "");
        empresa.put("estado", "");
        empresa.put("modalidad", "");
        empresa.put("sector", sector);
        empresa.put("sitioWeb", sitioWeb);
        empresa.put("descripcion", descripcion);
        empresa.put("carrerasAfines", carrerasAfines);
        empresa.put("calificacion", calificacion);
        empresa.put("totalResenas", totalResenas);
        empresa.put("convenioActivo", Boolean.FALSE);
        empresa.put("tecnologias", List.of());

        Map<String, Object> experiencia = experienciaDestacada(id);
        if (experiencia != null) {
            empresa.put("experienciaDestacada", experiencia);
        }

        return empresa;
    }

    private Map<String, Object> experienciaDestacada(Integer idEmpresa) {
        Object[] mejor = null;
        for (Object[] fila : resenaRepository.listarConAutor(idEmpresa)) {
            if (mejor == null
                    || ((Number) fila[1]).intValue() > ((Number) mejor[1]).intValue()) {
                mejor = fila;
            }
        }
        if (mejor == null) {
            return null;
        }

        String texto = mejor[3] != null && !mejor[3].toString().isBlank()
                ? mejor[3].toString()
                : mejor[2] == null ? "" : mejor[2].toString();
        if (texto.isBlank()) {
            return null;
        }

        Map<String, Object> exp = new HashMap<>();
        exp.put("autor", mejor[6]);
        exp.put("semestre", mejor[8] == null ? "" : mejor[8].toString());
        exp.put("texto", texto);
        return exp;
    }

}


