package com.tecmilenio.mapsconect.service;

import com.tecmilenio.mapsconect.entity.Notificacion;
import com.tecmilenio.mapsconect.entity.Usuario;
import com.tecmilenio.mapsconect.exception.ResourceNotFoundException;
import com.tecmilenio.mapsconect.repository.NotificacionRepository;
import com.tecmilenio.mapsconect.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Centro de notificaciones global (campana).
 * <p>
 * Es el único dueño del canal SSE por usuario y de la tabla persistida
 * {@code notificaciones}. Las notificaciones pueden provenir de cualquier área
 * del sistema (foro, círculos/sesiones, asesorías docentes, mensajes, etc.),
 * no solo de los mensajes de chat.
 */
@Service
public class NotificacionService {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FECHA_CORTA = DateTimeFormatter.ofPattern("dd MMM");

    // Canales de notificación por usuario (bell global, independiente de la conversación abierta).
    private final Map<Integer, List<SseEmitter>> emisoresPorUsuario = new ConcurrentHashMap<>();

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Canal SSE global por usuario: alimenta el centro de notificaciones (campana).
     */
    public SseEmitter notificacionesStream(String email) {
        Usuario usuario = obtenerUsuario(email);

        SseEmitter emitter = new SseEmitter(0L);
        List<SseEmitter> emisores = emisoresPorUsuario
                .computeIfAbsent(usuario.getId(), k -> new CopyOnWriteArrayList<>());
        emisores.add(emitter);

        Runnable limpiar = () -> {
            emisores.remove(emitter);
            if (emisores.isEmpty()) {
                emisoresPorUsuario.remove(usuario.getId());
            }
        };
        emitter.onCompletion(limpiar);
        emitter.onError(e -> limpiar.run());
        emitter.onTimeout(limpiar);

        try {
            emitter.send(SseEmitter.event().name("conectado").data("conexión establecida"));
        } catch (Exception e) {
            try {
                emitter.completeWithError(e);
            } catch (Exception ignorada) {
                // ya cerrado
            }
        }

        return emitter;
    }

    /**
     * Crea una notificación persistida para un usuario y la empuja en vivo por
     * SSE si tiene la campana abierta.
     *
     * @param tipo    identificador del área: mensaje, foro, duda, sesion, asesoria, ...
     * @param idOrigen identificador del elemento origen (publicación, sesión, ...) o null.
     */
    public void notificar(Integer idUsuario, String tipo, String titulo, String preview,
                          String enlace, Integer idOrigen) {
        Notificacion notificacion = Notificacion.builder()
                .idUsuario(idUsuario)
                .tipo(tipo)
                .titulo(titulo)
                .preview(preview)
                .enlace(enlace)
                .leida(false)
                .idOrigen(idOrigen)
                .fechaCreacion(LocalDateTime.now())
                .build();
        notificacionRepository.save(notificacion);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("tipo", tipo);
        payload.put("titulo", titulo);
        payload.put("preview", preview == null ? "" : preview);
        payload.put("enlace", enlace);
        payload.put("tiempo", HORA.format(LocalDateTime.now()));
        emitirEvento(idUsuario, tipo, payload);
    }

    /**
     * Emite un evento en vivo por SSE sin persistirlo. Se usa para eventos que
     * ya se derivan de datos existentes (p. ej. mensajes de chat), para no
     * duplicar la notificación en el resumen.
     */
    public void emitirEvento(Integer idUsuario, String nombreEvento, Map<String, Object> payload) {
        List<SseEmitter> emisores = emisoresPorUsuario.get(idUsuario);
        if (emisores == null || emisores.isEmpty()) {
            return;
        }

        List<SseEmitter> muertos = new ArrayList<>();
        for (SseEmitter emitter : emisores) {
            try {
                emitter.send(SseEmitter.event().name(nombreEvento).data(payload));
            } catch (Exception e) {
                muertos.add(emitter);
            }
        }
        emisores.removeAll(muertos);
        if (emisores.isEmpty()) {
            emisoresPorUsuario.remove(idUsuario);
        }
    }

    /**
     * Comprueba si un usuario tiene alguna conexión SSE activa (en línea).
     */
    public boolean estaEnLinea(Integer idUsuario) {
        if (idUsuario == null) return false;
        List<SseEmitter> emisores = emisoresPorUsuario.get(idUsuario);
        return emisores != null && !emisores.isEmpty();
    }

    /**
     * Notificaciones persistidas (de todas las áreas, excepto las que se derivan
     * de mensajes de chat) listas para el resumen del bell.
     */
    public List<Map<String, Object>> itemsPersistidos(Integer idUsuario) {
        List<Map<String, Object>> items = new ArrayList<>();
        for (Notificacion n : notificacionRepository.findTop30ByIdUsuarioOrderByFechaCreacionDesc(idUsuario)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", n.getId());
            item.put("tipo", n.getTipo());
            item.put("nombre", n.getTitulo());
            item.put("preview", n.getPreview() == null ? "" : n.getPreview());
            item.put("enlace", n.getEnlace());
            item.put("tiempo", formatearTiempo(n.getFechaCreacion()));
            item.put("noLeidos", Boolean.FALSE.equals(n.getLeida()) ? 1 : 0);
            items.add(item);
        }
        return items;
    }

    public long contarNoLeidas(Integer idUsuario) {
        return notificacionRepository.countByIdUsuarioAndLeidaFalse(idUsuario);
    }

    /**
     * Resumen de las notificaciones persistidas de todas las áreas
     * (foro, sesiones, asesorías, ...): {@code { noLeidos, items }}.
     */
    public Map<String, Object> resumenGenerales(String email) {
        Usuario usuario = obtenerUsuario(email);
        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("noLeidos", contarNoLeidas(usuario.getId()));
        resumen.put("items", itemsPersistidos(usuario.getId()));
        return resumen;
    }

    public void marcarTodasLeidas(Integer idUsuario) {
        notificacionRepository.marcarTodasLeidas(idUsuario);
    }

    private Usuario obtenerUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private String formatearTiempo(LocalDateTime fecha) {
        LocalDateTime hoy = LocalDateTime.now();
        if (fecha.toLocalDate().equals(hoy.toLocalDate())) {
            return fecha.format(HORA);
        }
        if (fecha.toLocalDate().equals(hoy.minusDays(1).toLocalDate())) {
            return "ayer";
        }
        long dias = ChronoUnit.DAYS.between(fecha.toLocalDate(), hoy.toLocalDate());
        if (dias < 7) {
            return dias + " d";
        }
        return fecha.format(FECHA_CORTA);
    }

}


