package com.tecmilenio.mapsconect.service;

import com.tecmilenio.mapsconect.dto.ConversacionDTO;
import com.tecmilenio.mapsconect.dto.DetalleConversacionDTO;
import com.tecmilenio.mapsconect.dto.MensajeDTO;
import com.tecmilenio.mapsconect.entity.Conversacion;
import com.tecmilenio.mapsconect.entity.Mensaje;
import com.tecmilenio.mapsconect.entity.Usuario;
import com.tecmilenio.mapsconect.exception.ResourceNotFoundException;
import com.tecmilenio.mapsconect.repository.ConversacionRepository;
import com.tecmilenio.mapsconect.repository.MensajeRepository;
import com.tecmilenio.mapsconect.repository.UsuarioRepository;
import com.tecmilenio.mapsconect.util.ArchivoSeguro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Servicio de mensajería privada (chat 1 a 1).
 *
 * <p>Gestiona conversaciones entre pares de usuarios, envío de mensajes de
 * texto e imágenes adjuntas, marcaje de lectura y un canal SSE por
 * conversación para entregar mensajes en tiempo real al frontend.</p>
 *
 * <p>También mantiene un canal SSE global por usuario (campana de
 * notificaciones) y un resumen de no leídos para el badge.</p>
 *
 * <p>Los archivos adjuntos se validan con {@link ArchivoSeguro} y se
 * almacenan en {@code uploads/adjuntos/<id_mensaje>}.</p>
 *
 * @see NotificacionService
 */
@Service
public class MensajeService {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FECHA_CORTA = DateTimeFormatter.ofPattern("dd MMM");

    private final Map<Integer, List<SseEmitter>> emisoresPorConversacion = new ConcurrentHashMap<>();

    @Autowired
    private ConversacionRepository conversacionRepository;

    @Autowired
    private MensajeRepository mensajeRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    @org.springframework.context.annotation.Lazy
    private NotificacionService notificacionService;

    @Autowired
    private com.tecmilenio.mapsconect.storage.AlmacenamientoService almacenamientoService;

    @Autowired
    @org.springframework.context.annotation.Lazy
    private com.tecmilenio.mapsconect.messaging.MensajeriaDistribuidaService mensajeriaDistribuidaService;

    /**
     * Lista las conversaciones del usuario con paginación.
     *
     * @param email email del usuario autenticado
     * @param page  página (0-indexed)
     * @param size  conversaciones por página (max 100)
     */
    public List<ConversacionDTO> listarConversaciones(String email, int page, int size) {
        Usuario usuario = obtenerUsuario(email);
        int safeSize = Math.min(size, 100);
        PageRequest pageable = PageRequest.of(page, safeSize);

        return conversacionRepository
                .findByUsuario1IdOrUsuario2IdOrderByFechaInicioDesc(usuario.getId(), usuario.getId(), pageable)
                .stream()
                .map(conv -> mapearConversacion(conv, usuario))
                .collect(Collectors.toList());
    }

    /**
     * Lista todas las conversaciones del usuario sin paginar.
     * Uso interno (resumenNotificaciones, marcarTodasLeidas).
     */
    private List<Conversacion> listarTodasConversacionesEntidad(Integer idUsuario) {
        return conversacionRepository
                .findByUsuario1IdOrUsuario2IdOrderByFechaInicioDesc(idUsuario, idUsuario);
    }

    @Transactional
    public Integer crearConversacion(String email, Integer idUsuario) {
        Usuario usuario = obtenerUsuario(email);
        Usuario otro = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (usuario.getId().equals(idUsuario)) {
            throw new IllegalArgumentException("No puedes iniciar una conversación contigo mismo");
        }

        List<Conversacion> existentes = conversacionRepository
                .findByUsuario1IdOrUsuario2IdOrderByFechaInicioDesc(usuario.getId(), usuario.getId());
        for (Conversacion conv : existentes) {
            Integer u1 = conv.getUsuario1().getId();
            Integer u2 = conv.getUsuario2().getId();
            if ((u1.equals(usuario.getId()) && u2.equals(idUsuario))
                    || (u1.equals(idUsuario) && u2.equals(usuario.getId()))) {
                return conv.getId();
            }
        }

        Conversacion conversacion;
        if (usuario.getId() < idUsuario) {
            conversacion = Conversacion.builder()
                    .usuario1(usuario)
                    .usuario2(otro)
                    .fechaInicio(LocalDateTime.now())
                    .build();
        } else {
            conversacion = Conversacion.builder()
                    .usuario1(otro)
                    .usuario2(usuario)
                    .fechaInicio(LocalDateTime.now())
                    .build();
        }

        return conversacionRepository.save(conversacion).getId();
    }

    /**
     * Devuelve el detalle de una conversación con sus mensajes paginados.
     *
     * <p>Los mensajes se ordenan cronológicamente dentro de la página.
     * Por defecto carga los últimos 50 (page=0 en orden inverso).
     * Para scroll infinito hacia atrás el cliente pide page=1, page=2, etc.</p>
     *
     * @param email          email del usuario autenticado
     * @param idConversacion id de la conversación
     * @param page           página de mensajes (0 = más recientes)
     * @param size           mensajes por página (máx. 100)
     */
    public DetalleConversacionDTO obtenerDetalle(String email, Integer idConversacion, int page, int size) {
        Usuario usuario = obtenerUsuario(email);
        Conversacion conversacion = obtenerConversacionAccesible(idConversacion, usuario);

        Usuario otro = esParticipante(conversacion, usuario.getId()) == 1
                ? conversacion.getUsuario2() : conversacion.getUsuario1();

        int safeSize = Math.min(size, 100);
        // Pedimos en orden DESC para obtener los más recientes primero,
        // luego invertimos para presentar cronológicamente al cliente.
        PageRequest pageable = PageRequest.of(page, safeSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<Mensaje> paginaMensajes = mensajeRepository
                .findByIdConversacionOrderByIdAsc(idConversacion, pageable);

        List<MensajeDTO> mensajesDTO = paginaMensajes.getContent().stream()
                .sorted((a, b) -> a.getId().compareTo(b.getId()))   // orden cronológico
                .map(m -> mapearMensaje(m, usuario))
                .collect(Collectors.toList());

        return DetalleConversacionDTO.builder()
                .id(conversacion.getId())
                .idUsuario(otro.getId())
                .nombre(otro.getNombreCompleto())
                .subtitulo(otro.getRol().name())
                .foto(otro.getFotoUrl())
                .esProf(otro.getRol() == Usuario.Rol.PROFESOR)
                .enLinea(notificacionService.estaEnLinea(otro.getId()))
                .mensajes(mensajesDTO)
                .pagina(page)
                .hayMas(paginaMensajes.hasNext())
                .totalMensajes(paginaMensajes.getTotalElements())
                .build();
    }

    /**
     * Sobrecarga sin paginación explícita: carga los últimos 50 mensajes.
     * Mantiene compatibilidad con el código interno que llama a obtenerDetalle(email, id).
     */
    public DetalleConversacionDTO obtenerDetalle(String email, Integer idConversacion) {
        return obtenerDetalle(email, idConversacion, 0, 50);
    }

    public MensajeDTO enviarMensaje(String email, Integer idConversacion, String texto) {
        Usuario usuario = obtenerUsuario(email);
        Conversacion conversacion = obtenerConversacionAccesible(idConversacion, usuario);
        Integer idOtro = esParticipante(conversacion, usuario.getId()) == 1
                ? conversacion.getUsuario2().getId() : conversacion.getUsuario1().getId();

        Mensaje mensaje = Mensaje.builder()
                .idConversacion(idConversacion)
                .idEmisor(usuario.getId())
                .contenido(texto)
                .fechaEnvio(LocalDateTime.now())
                .leido(false)
                .build();

        Mensaje guardado = mensajeRepository.save(mensaje);
        MensajeDTO dto = mapearMensaje(guardado, usuario);

        mensajeriaDistribuidaService.publicarMensajeChat(idConversacion, dto, idOtro);
        return dto;
    }

    @Transactional
    public MensajeDTO adjuntarArchivo(String email, Integer idConversacion, String texto, MultipartFile archivo) {
        Usuario usuario = obtenerUsuario(email);
        Conversacion conversacion = obtenerConversacionAccesible(idConversacion, usuario);
        Integer idOtro = esParticipante(conversacion, usuario.getId()) == 1
                ? conversacion.getUsuario2().getId() : conversacion.getUsuario1().getId();

        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("Selecciona un archivo para adjuntar");
        }

        ArchivoSeguro.validar(archivo.getOriginalFilename(), archivo.getSize());
        String nombreLimpio = ArchivoSeguro.limpiarNombre(archivo.getOriginalFilename());

        Mensaje mensaje = Mensaje.builder()
                .idConversacion(idConversacion)
                .idEmisor(usuario.getId())
                .contenido(texto == null ? "" : texto)
                .adjuntoNombre(nombreLimpio)
                .adjuntoTipo(ArchivoSeguro.mimeDesdeNombre(nombreLimpio))
                .adjuntoTamano(archivo.getSize())
                .fechaEnvio(LocalDateTime.now())
                .leido(false)
                .build();

        Mensaje guardado = mensajeRepository.save(mensaje);

        try {
            almacenamientoService.guardar("adjuntos/" + guardado.getId(), archivo.getBytes(), mensaje.getAdjuntoTipo());
        } catch (IOException e) {
            throw new IllegalArgumentException("No se pudo guardar el archivo adjunto: " + e.getMessage());
        }

        MensajeDTO dto = mapearMensaje(guardado, usuario);
        mensajeriaDistribuidaService.publicarMensajeChat(idConversacion, dto, idOtro);
        return dto;
    }

    public AdjuntoDescarga obtenerAdjunto(String email, Integer idMensaje) {
        Usuario usuario = obtenerUsuario(email);
        Mensaje mensaje = mensajeRepository.findById(idMensaje)
                .orElseThrow(() -> new ResourceNotFoundException("Mensaje no encontrado"));

        obtenerConversacionAccesible(mensaje.getIdConversacion(), usuario);

        if (mensaje.getAdjuntoNombre() == null || mensaje.getAdjuntoTamano() == null) {
            throw new ResourceNotFoundException("Este mensaje no tiene archivo adjunto");
        }

        try {
            byte[] bytes = almacenamientoService.descargar("adjuntos/" + mensaje.getId());
            return new AdjuntoDescarga(bytes, mensaje.getAdjuntoNombre(), mensaje.getAdjuntoTipo() != null ? mensaje.getAdjuntoTipo() : "application/octet-stream");
        } catch (IOException e) {
            throw new ResourceNotFoundException("El archivo adjunto no está disponible");
        }
    }

    /**
     * Marca como leídos todos los mensajes recibidos de una conversación.
     *
     * <p>Usa un UPDATE masivo directo en BD ({@code @Modifying}) en lugar de
     * cargar toda la lista en RAM y hacer saveAll. Es atómico y mucho más
     * eficiente en conversaciones largas.</p>
     */
    @Transactional
    public void marcarLeidos(String email, Integer idConversacion) {
        Usuario usuario = obtenerUsuario(email);
        obtenerConversacionAccesible(idConversacion, usuario);
        mensajeRepository.marcarLeidosMasivo(idConversacion, usuario.getId());
    }

    public SseEmitter suscribirse(Integer idConversacion, String email) {
        Usuario usuario = obtenerUsuario(email);
        obtenerConversacionAccesible(idConversacion, usuario);

        SseEmitter emitter = crearEmitterConCleanup(emisoresPorConversacion, idConversacion);

        return emitter;
    }

    public void emitirTyping(Integer idConversacion, String email, boolean escribiendo) {
        Usuario usuario = obtenerUsuario(email);
        obtenerConversacionAccesible(idConversacion, usuario);

        mensajeriaDistribuidaService.publicarTyping(idConversacion, email, escribiendo);
    }

    public void despacharMensajeLocal(Integer idConversacion, MensajeDTO mensaje) {
        emitirEvento(emisoresPorConversacion, idConversacion, "mensaje", mensaje);
    }

    public void despacharNotificacionDestinatarioLocal(Integer idUsuario, Integer idConversacion, MensajeDTO mensaje) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("tipo", "mensaje");
        payload.put("conversacionId", idConversacion);
        payload.put("emisorId", mensaje.getAutorId());
        payload.put("emisorNombre", mensaje.getAutorNombre());
        payload.put("mensaje", mensaje);
        payload.put("enlace", "mensajes.html?conv=" + idConversacion);

        notificacionService.emitirEventoDirecto(idUsuario, "mensaje", payload);
    }

    public void despacharTypingLocal(Integer idConversacion, String email, boolean escribiendo) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        if (usuario == null) return;

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("idConversacion", idConversacion);
        payload.put("idUsuario", usuario.getId());
        payload.put("nombre", usuario.getNombreCompleto());
        payload.put("escribiendo", escribiendo);

        emitirEvento(emisoresPorConversacion, idConversacion, "typing", payload);
    }

    /**
     * Envía un evento SSE a todos los emitters activos de un canal.
     *
     * <p>Acumula los emitters que fallaron en una lista separada ({@code muertos})
     * y los elimina <em>después</em> del loop de envío — igual que
     * {@code NotificacionService#emitirEventoDirecto} — evitando así la
     * modificación de la colección durante la iteración.</p>
     */
    private void emitirEvento(Map<Integer, List<SseEmitter>> canal, Integer clave, String nombre, Object data) {
        List<SseEmitter> emisores = canal.get(clave);
        if (emisores == null || emisores.isEmpty()) {
            return;
        }

        List<SseEmitter> muertos = new ArrayList<>();
        for (SseEmitter emitter : emisores) {
            try {
                emitter.send(SseEmitter.event().name(nombre).data(data));
            } catch (Exception e) {
                muertos.add(emitter);
            }
        }

        // Limpiar fuera del loop para no modificar mientras iteramos
        for (SseEmitter muerto : muertos) {
            emisores.remove(muerto);
            try {
                muerto.completeWithError(new IllegalStateException("Emitter desconectado"));
            } catch (Exception ignorada) {
                // ya cerrado
            }
        }

        if (emisores.isEmpty()) {
            canal.remove(clave);
        }
    }


    /**
     * Crea un SseEmitter con cleanup automático (onCompletion/onError/onTimeout)
     * y lo registra en el canal especificado. Envía un evento de conexión
     * exitosa al emitter recién creado.
     *
     * @param canal  mapa de emisores por clave (conversación o usuario)
     * @param clave  id de la conversación o usuario
     * @return un SseEmitter listo para ser devuelto al cliente
     */
    private SseEmitter crearEmitterConCleanup(Map<Integer, List<SseEmitter>> canal, Integer clave) {
        SseEmitter emitter = new SseEmitter(0L);
        List<SseEmitter> emisores = canal
                .computeIfAbsent(clave, k -> new CopyOnWriteArrayList<>());
        emisores.add(emitter);

        Runnable limpiar = () -> {
            emisores.remove(emitter);
            if (emisores.isEmpty()) {
                canal.remove(clave);
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
     * Resumen de no leídos por conversación, para el badge y el panel de la campana.
     * Carga todas las conversaciones del usuario (sin límite) para calcular el total global.
     */
    public Map<String, Object> resumenNotificaciones(String email) {
        Usuario usuario = obtenerUsuario(email);
        List<Conversacion> conversaciones = listarTodasConversacionesEntidad(usuario.getId());

        List<Map<String, Object>> items = new ArrayList<>();
        long totalNoLeidos = 0;
        for (Conversacion conv : conversaciones) {
            ConversacionDTO dto = mapearConversacion(conv, usuario);
            long noLeidos = mensajeRepository.countNoLeidos(conv.getId(), usuario.getId());
            if (noLeidos > 0) {
                totalNoLeidos += noLeidos;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", conv.getId());
            item.put("idUsuario", dto.getIdUsuario());
            item.put("nombre", dto.getNombre());
            item.put("foto", dto.getFoto());
            item.put("esProf", dto.isEsProf());
            item.put("preview", dto.getPreview());
            item.put("tiempo", dto.getTiempo());
            item.put("noLeidos", noLeidos);
            item.put("enlace", "mensajes.html?conv=" + conv.getId());
            items.add(item);
        }

        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("noLeidos", totalNoLeidos);
        resumen.put("items", items);
        return resumen;
    }


    @Transactional
    public void marcarTodasLeidas(String email) {
        Usuario usuario = obtenerUsuario(email);
        for (Object[] fila : mensajeRepository.resumenNoLeidos(usuario.getId())) {
            Integer idConversacion = ((Number) fila[0]).intValue();
            marcarLeidos(email, idConversacion);
        }
    }

    private Usuario obtenerUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private Conversacion obtenerConversacionAccesible(Integer idConversacion, Usuario usuario) {
        Conversacion conversacion = conversacionRepository.findById(idConversacion)
                .orElseThrow(() -> new ResourceNotFoundException("Conversación no encontrada"));

        if (esParticipante(conversacion, usuario.getId()) == 0) {
            throw new IllegalArgumentException("No tienes acceso a esta conversación");
        }
        return conversacion;
    }

    private int esParticipante(Conversacion conversacion, Integer idUsuario) {
        if (conversacion.getUsuario1().getId().equals(idUsuario)) return 1;
        if (conversacion.getUsuario2().getId().equals(idUsuario)) return 2;
        return 0;
    }

    private ConversacionDTO mapearConversacion(Conversacion conversacion, Usuario usuarioActual) {
        Usuario otro = esParticipante(conversacion, usuarioActual.getId()) == 1
                ? conversacion.getUsuario2() : conversacion.getUsuario1();

        Optional<Mensaje> ultimo = mensajeRepository.findFirstByIdConversacionOrderByIdDesc(conversacion.getId());

        String preview = ultimo.map(m -> {
            String texto = m.getContenido();
            String base;
            if (texto != null && !texto.isBlank()) {
                base = texto;
            } else if (m.getAdjuntoNombre() != null) {
                base = "Adjunto: " + m.getAdjuntoNombre();
            } else {
                base = "";
            }
            return truncar(base, 60);
        }).orElse("");
        String tiempo = ultimo.map(m -> formatearTiempo(m.getFechaEnvio())).orElse("");

        return ConversacionDTO.builder()
                .id(conversacion.getId())
                .idUsuario(otro.getId())
                .nombre(otro.getNombreCompleto())
                .rol(otro.getRol().name())
                .foto(otro.getFotoUrl())
                .esProf(otro.getRol() == Usuario.Rol.PROFESOR)
                .preview(preview)
                .tiempo(tiempo)
                .fechaUltimoMensaje(ultimo.map(m -> m.getFechaEnvio().toString()).orElse(null))
                .enLinea(notificacionService.estaEnLinea(otro.getId()))
                .build();
    }

    private MensajeDTO mapearMensaje(Mensaje mensaje, Usuario usuarioActual) {
        String autorNombre = "";
        Usuario autor = usuarioRepository.findById(mensaje.getIdEmisor()).orElse(null);
        if (autor != null) {
            autorNombre = autor.getNombreCompleto();
        }

        return MensajeDTO.builder()
                .id(mensaje.getId())
                .texto(mensaje.getContenido())
                .tiempo(formatearTiempo(mensaje.getFechaEnvio()))
                .fechaEnvio(mensaje.getFechaEnvio().toString())
                .propio(mensaje.getIdEmisor().equals(usuarioActual.getId()))
                .leido(mensaje.getLeido())
                .autorNombre(autorNombre)
                .autorId(mensaje.getIdEmisor())
                .adjuntoNombre(mensaje.getAdjuntoNombre())
                .adjuntoTipo(mensaje.getAdjuntoTipo())
                .adjuntoTamano(mensaje.getAdjuntoTamano())
                .build();
    }

    public static class AdjuntoDescarga {
        private final byte[] contenido;
        private final String nombre;
        private final String tipo;

        public AdjuntoDescarga(byte[] contenido, String nombre, String tipo) {
            this.contenido = contenido;
            this.nombre = nombre;
            this.tipo = tipo;
        }

        public byte[] getContenido() {
            return contenido;
        }

        public String getNombre() {
            return nombre;
        }

        public String getTipo() {
            return tipo;
        }
    }

    private String truncar(String texto, int max) {
        if (texto == null || texto.length() <= max) {
            return texto == null ? "" : texto;
        }
        return texto.substring(0, max) + "…";
    }

    private String formatearTiempo(LocalDateTime fecha) {
        LocalDateTime hoy = LocalDateTime.now();
        if (fecha.toLocalDate().equals(hoy.toLocalDate())) {
            return fecha.format(HORA);
        }
        if (fecha.toLocalDate().equals(hoy.minusDays(1).toLocalDate())) {
            return "ayer";
        }
        return fecha.format(FECHA_CORTA);
    }

}

