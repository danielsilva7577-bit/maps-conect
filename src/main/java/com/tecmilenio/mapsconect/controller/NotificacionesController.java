package com.tecmilenio.mapsconect.controller;

import com.tecmilenio.mapsconect.exception.ResourceNotFoundException;

import com.tecmilenio.mapsconect.dto.ApiResponse;
import com.tecmilenio.mapsconect.entity.Usuario;
import com.tecmilenio.mapsconect.repository.UsuarioRepository;
import com.tecmilenio.mapsconect.service.MensajeService;
import com.tecmilenio.mapsconect.service.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador de notificaciones push.
 *
 * <p>Combina notificaciones de chat (derivadas de {@link MensajeService})
 * con notificaciones de otras áreas de la plataforma (foro, recursos, tips)
 * gestionadas por {@link NotificacionService}.</p>
 *
 * <p>El endpoint SSE {@code /notificaciones/stream} permite al frontend
 * recibir notificaciones en tiempo real mediante {@code EventSource}.</p>
 *
 * @see MensajeService
 * @see NotificacionService
 */
@RestController
@RequestMapping("/notificaciones")
public class NotificacionesController {

    @Autowired
    private MensajeService mensajeService;

    @Autowired
    private NotificacionService notificacionService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/resumen")
    public ResponseEntity<ApiResponse<Map<String, Object>>> resumen(Authentication authentication) {
        // Mensajes de chat (derivados) + notificaciones persistidas de otras áreas.
        Map<String, Object> resumenMensajes = mensajeService.resumenNotificaciones(authentication.getName());
        Map<String, Object> resumenGenerales = notificacionService.resumenGenerales(authentication.getName());

        List<Map<String, Object>> items = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> itemsMensajes = (List<Map<String, Object>>) resumenMensajes.getOrDefault("items", List.of());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> itemsGenerales = (List<Map<String, Object>>) resumenGenerales.getOrDefault("items", List.of());
        items.addAll(itemsMensajes);
        items.addAll(itemsGenerales);

        long noLeidos = ((Number) resumenMensajes.getOrDefault("noLeidos", 0)).longValue()
                + ((Number) resumenGenerales.getOrDefault("noLeidos", 0)).longValue();

        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("noLeidos", noLeidos);
        resumen.put("items", items);
        return ResponseEntity.ok(ApiResponse.success(resumen, "Resumen de notificaciones"));
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNotificaciones(Authentication authentication) {
        return notificacionService.notificacionesStream(authentication.getName());
    }

    @PostMapping("/marcar-todas")
    public ResponseEntity<ApiResponse<Void>> marcarTodas(Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        mensajeService.marcarTodasLeidas(authentication.getName());
        notificacionService.marcarTodasLeidas(usuario.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Notificaciones marcadas como leídas"));
    }
}
