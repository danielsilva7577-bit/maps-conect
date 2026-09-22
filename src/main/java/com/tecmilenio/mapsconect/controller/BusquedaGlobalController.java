package com.tecmilenio.mapsconect.controller;

import com.tecmilenio.mapsconect.dto.ApiResponse;
import com.tecmilenio.mapsconect.dto.BusquedaGlobalDTO;
import com.tecmilenio.mapsconect.entity.Carrera;
import com.tecmilenio.mapsconect.entity.Estudiante;
import com.tecmilenio.mapsconect.entity.Materia;
import com.tecmilenio.mapsconect.entity.Usuario;
import com.tecmilenio.mapsconect.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador de busqueda global unificada.
 * Busca simultaneamente personas, materias, recursos/apuntes y dudas del foro.
 */
@Tag(name = "Busqueda Global", description = "Busqueda unificada en toda la plataforma")
@RestController
@RequestMapping("/busqueda")
public class BusquedaGlobalController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private CarreraRepository carreraRepository;

    @Autowired
    private MateriaRepository materiaRepository;

    @Autowired
    private RecursoAcademicoRepository recursoRepository;

    @Autowired
    private PublicacionRepository publicacionRepository;

    @Operation(summary = "Busqueda global unificada", description = "Busca en personas, materias, apuntes y foro con un solo termino.")
    @GetMapping("/global")
    public ResponseEntity<ApiResponse<BusquedaGlobalDTO>> buscarGlobal(
            @RequestParam(value = "q", required = false) String q) {

        String query = q == null ? "" : q.trim();
        BusquedaGlobalDTO resultado = new BusquedaGlobalDTO();

        if (query.length() < 2) {
            return ResponseEntity.ok(ApiResponse.success(resultado, "Escribe al menos 2 caracteres"));
        }

        // 1. Personas (alumnos y profesores)
        List<Map<String, Object>> personas = new ArrayList<>();
        for (Usuario u : usuarioRepository
                .findTop5ByActivoTrueAndNombreCompletoContainingIgnoreCaseOrderByPuntosReputacionDesc(query)) {
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("id", u.getId());
            p.put("nombre", u.getNombreCompleto());
            p.put("foto", u.getFotoUrl());
            p.put("rol", u.getRol().name());

            String sub = u.getRol() == Usuario.Rol.PROFESOR ? "Docente" : "Estudiante";
            if (u.getRol() == Usuario.Rol.ESTUDIANTE) {
                Estudiante est = estudianteRepository.findByIdUsuario(u.getId()).orElse(null);
                if (est != null) {
                    String carrera = est.getIdCarrera() != null
                            ? carreraRepository.findById(est.getIdCarrera()).map(Carrera::getNombre).orElse("")
                            : "";
                    String sem = est.getSemestreActual() != null ? est.getSemestreActual() + "° Semestre" : "";
                    if (!carrera.isEmpty() && !sem.isEmpty()) {
                        sub = carrera + " • " + sem;
                    } else if (!carrera.isEmpty()) {
                        sub = carrera;
                    }
                }
            }
            p.put("subtitulo", sub);
            p.put("enlace", "usuario.html?id=" + u.getId());
            personas.add(p);
        }
        resultado.setPersonas(personas);

        // 2. Materias
        List<Map<String, Object>> materias = new ArrayList<>();
        for (Materia m : materiaRepository.findTop5ByNombreContainingIgnoreCaseOrderByNombreAsc(query)) {
            Map<String, Object> mat = new LinkedHashMap<>();
            mat.put("id", m.getId());
            mat.put("nombre", m.getNombre());
            mat.put("enlace", "comunidad.html#foro-dudas");
            materias.add(mat);
        }
        resultado.setMaterias(materias);

        // 3. Recursos / Apuntes
        List<Map<String, Object>> recursos = new ArrayList<>();
        for (Object[] fila : recursoRepository.buscarRecursosGlobal(query)) {
            Map<String, Object> rec = new LinkedHashMap<>();
            rec.put("id", fila[0]);
            rec.put("titulo", fila[1]);
            rec.put("descripcion", fila[2]);
            rec.put("materia", fila[3] != null ? fila[3] : "General");
            rec.put("autor", fila[4]);
            rec.put("tipoArchivo", fila[5]);
            rec.put("enlace", "comunidad.html#apuntes");
            recursos.add(rec);
        }
        resultado.setRecursos(recursos);

        // 4. Foro / Dudas
        List<Map<String, Object>> foro = new ArrayList<>();
        for (Object[] fila : publicacionRepository.buscarPublicacionesGlobal(query)) {
            Map<String, Object> pub = new LinkedHashMap<>();
            pub.put("id", fila[0]);
            pub.put("titulo", fila[1]);
            pub.put("descripcion", fila[2]);
            pub.put("materia", fila[3] != null ? fila[3] : "General");
            pub.put("autor", fila[4]);
            pub.put("enlace", "comunidad.html#foro-dudas");
            foro.add(pub);
        }
        resultado.setForo(foro);

        return ResponseEntity.ok(ApiResponse.success(resultado, "Resultados de busqueda"));
    }

}
