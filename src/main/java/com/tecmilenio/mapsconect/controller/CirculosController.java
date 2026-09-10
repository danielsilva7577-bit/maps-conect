package com.tecmilenio.mapsconect.controller;

import com.tecmilenio.mapsconect.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/circulos")
public class CirculosController {

    private final AtomicInteger sessionId = new AtomicInteger(1);
    private final List<Map<String, Object>> sesiones = new ArrayList<>();
    private final List<Map<String, Object>> misGrupos = new ArrayList<>();

    public CirculosController() {
        seedData();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> listar(@RequestParam(name = "busqueda", required = false) String busqueda) {
        String filtro = busqueda == null ? "" : busqueda.trim().toLowerCase();

        List<Map<String, Object>> sesionesFiltradas = sesiones.stream()
                .filter(s -> filtro.isEmpty()
                        || String.valueOf(s.getOrDefault("titulo", "")).toLowerCase().contains(filtro)
                        || String.valueOf(s.getOrDefault("materia", "")).toLowerCase().contains(filtro)
                        || String.valueOf(s.getOrDefault("organizador", "")).toLowerCase().contains(filtro))
                .toList();

        List<Map<String, Object>> gruposFiltrados = misGrupos.stream()
                .filter(g -> filtro.isEmpty()
                        || String.valueOf(g.getOrDefault("nombre", "")).toLowerCase().contains(filtro)
                        || String.valueOf(g.getOrDefault("descripcion", "")).toLowerCase().contains(filtro))
                .toList();

        Map<String, Object> payload = new HashMap<>();
        payload.put("sesiones", sesionesFiltradas);
        payload.put("misGrupos", gruposFiltrados);

        return ResponseEntity.ok(ApiResponse.success(payload, "Círculos cargados correctamente"));
    }

    @PostMapping("/sesiones")
    public ResponseEntity<ApiResponse<Map<String, Object>>> crearSesion(@RequestBody Map<String, Object> payload) {
        String titulo = asString(payload.get("titulo"));
        String materia = asString(payload.get("materia"));
        String fecha = asString(payload.get("fecha"));
        String horaInicio = asString(payload.get("horaInicio"));

        if (titulo.isBlank() || materia.isBlank() || fecha.isBlank() || horaInicio.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Faltan datos obligatorios para crear la sesión"));
        }

        Map<String, Object> sesion = new HashMap<>();
        int id = sessionId.getAndIncrement();
        sesion.put("id", id);
        sesion.put("titulo", titulo);
        sesion.put("materia", materia);
        sesion.put("descripcion", asString(payload.get("descripcion")));
        sesion.put("modalidad", asString(payload.get("modalidad"), "Grupal"));
        sesion.put("ubicacion", asString(payload.get("ubicacion"), "En línea"));
        sesion.put("fecha", fecha);
        sesion.put("horaInicio", horaInicio);
        sesion.put("duracionMin", toInt(payload.get("duracionMin"), 90));
        sesion.put("cupoMax", toInt(payload.get("cupoMax"), 30));
        sesion.put("estado", "ABIERTA");
        sesion.put("organizador", "Tú");
        sesion.put("organizadorId", 1);
        sesion.put("inscritos", 1);
        sesion.put("inscrito", true);
        sesion.put("organizadorYo", true);

        sesiones.add(0, sesion);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(sesion, "Sesión creada correctamente"));
    }

    @PostMapping("/sesiones/{id}/inscribirse")
    public ResponseEntity<ApiResponse<Map<String, Object>>> inscribirse(@PathVariable Integer id) {
        Map<String, Object> sesion = buscarSesionPorId(id);
        if (sesion == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "La sesión no existe"));
        }

        boolean yaInscrito = Boolean.TRUE.equals(sesion.get("inscrito"));
        if (yaInscrito) {
            return ResponseEntity.ok(ApiResponse.success(sesion, "Ya estabas inscrito en esta sesión"));
        }

        int inscritos = toInt(sesion.get("inscritos"), 0);
        int cupo = toInt(sesion.get("cupoMax"), 30);
        if (inscritos >= cupo) {
            sesion.put("estado", "CERRADA");
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "La sesión ya alcanzó su cupo máximo"));
        }

        sesion.put("inscrito", true);
        sesion.put("inscritos", inscritos + 1);
        if (toInt(sesion.get("inscritos"), 0) >= cupo) {
            sesion.put("estado", "CERRADA");
        }

        return ResponseEntity.ok(ApiResponse.success(sesion, "Inscripción confirmada"));
    }

    @DeleteMapping("/sesiones/{id}/inscribirse")
    public ResponseEntity<ApiResponse<Map<String, Object>>> desinscribirse(@PathVariable Integer id) {
        Map<String, Object> sesion = buscarSesionPorId(id);
        if (sesion == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "La sesión no existe"));
        }

        if (!Boolean.TRUE.equals(sesion.get("inscrito"))) {
            return ResponseEntity.ok(ApiResponse.success(sesion, "No estabas inscrito en esta sesión"));
        }

        int inscritos = toInt(sesion.get("inscritos"), 0);
        sesion.put("inscrito", false);
        sesion.put("inscritos", Math.max(0, inscritos - 1));
        sesion.put("estado", "ABIERTA");

        return ResponseEntity.ok(ApiResponse.success(sesion, "Te has dado de baja de la sesión"));
    }

    private Map<String, Object> buscarSesionPorId(Integer id) {
        for (Map<String, Object> sesion : sesiones) {
            if (id.equals(sesion.get("id"))) {
                return sesion;
            }
        }
        return null;
    }

    private void seedData() {
        Map<String, Object> sesion1 = new HashMap<>();
        sesion1.put("id", sessionId.getAndIncrement());
        sesion1.put("titulo", "Repaso de SQL para parcial");
        sesion1.put("materia", "Bases de datos");
        sesion1.put("descripcion", "Practica consultas, joins y normalización con ejercicios guiados.");
        sesion1.put("modalidad", "En línea");
        sesion1.put("ubicacion", "Google Meet");
        sesion1.put("fecha", "2026-09-15");
        sesion1.put("horaInicio", "18:00");
        sesion1.put("duracionMin", 90);
        sesion1.put("cupoMax", 12);
        sesion1.put("estado", "ABIERTA");
        sesion1.put("organizador", "Diana López");
        sesion1.put("organizadorId", 25);
        sesion1.put("inscritos", 8);
        sesion1.put("inscrito", false);
        sesion1.put("organizadorYo", false);
        sesiones.add(sesion1);

        Map<String, Object> sesion2 = new HashMap<>();
        sesion2.put("id", sessionId.getAndIncrement());
        sesion2.put("titulo", "Taller de reactivo de cálculo");
        sesion2.put("materia", "Cálculo diferencial");
        sesion2.put("descripcion", "Resolver dudas de límites, derivadas y problemas tipo examen.");
        sesion2.put("modalidad", "Presencial");
        sesion2.put("ubicacion", "Biblioteca Central - Sala 3");
        sesion2.put("fecha", "2026-09-18");
        sesion2.put("horaInicio", "17:30");
        sesion2.put("duracionMin", 120);
        sesion2.put("cupoMax", 20);
        sesion2.put("estado", "ABIERTA");
        sesion2.put("organizador", "Sofía Ruiz");
        sesion2.put("organizadorId", 18);
        sesion2.put("inscritos", 15);
        sesion2.put("inscrito", true);
        sesion2.put("organizadorYo", false);
        sesiones.add(sesion2);

        Map<String, Object> grupo1 = new HashMap<>();
        grupo1.put("id", 1);
        grupo1.put("nombre", "Grupo de estudio de IA");
        grupo1.put("descripcion", "Revisamos temas de machine learning, ejemplos y tareas.");
        grupo1.put("miembros", 6);
        misGrupos.add(grupo1);

        Map<String, Object> grupo2 = new HashMap<>();
        grupo2.put("id", 2);
        grupo2.put("nombre", "Álgebra lineal team");
        grupo2.put("descripcion", "Preparación para exámenes y resolución de ejercicios de práctica.");
        grupo2.put("miembros", 4);
        misGrupos.add(grupo2);
    }

    private String asString(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String asString(Object value, String defaultValue) {
        String text = asString(value);
        return text.isEmpty() ? defaultValue : text;
    }

    private int toInt(Object value, int fallback) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
