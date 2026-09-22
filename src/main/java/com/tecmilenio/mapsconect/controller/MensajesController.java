package com.tecmilenio.mapsconect.controller;

import com.tecmilenio.mapsconect.dto.ApiResponse;
import com.tecmilenio.mapsconect.dto.ConversacionDTO;
import com.tecmilenio.mapsconect.dto.DetalleConversacionDTO;
import com.tecmilenio.mapsconect.dto.MensajeDTO;
import com.tecmilenio.mapsconect.dto.MensajeRequestDTO;
import com.tecmilenio.mapsconect.security.RateLimited;
import com.tecmilenio.mapsconect.service.MensajeService;
import com.tecmilenio.mapsconect.util.ArchivoSeguro;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * Controlador de mensajería privada (chat 1 a 1).
 *
 * <p>Expone endpoints REST para conversaciones entre estudiantes y profesores,
 * así como un endpoint SSE de streaming en tiempo real para recibir nuevos
 * mensajes sin polling.</p>
 *
 * <p>Los endpoints que crean o envían contenido están protegidos con
 * {@code @RateLimited(scope = "mensajes")} para prevenir abusos.</p>
 *
 * <p>Los SSE se consumen desde el frontend mediante {@code EventSource}
 * con reconexión automática (backoff exponencial).</p>
 *
 * <p>El token JWT se pasa por query param {@code ?token=...} en la ruta SSE
 * (el frontend de browser no permite headers personalizados en EventSource).</p>
 *
 * @see MensajeService
 */
@Tag(name = "Mensajes", description = "Chat 1 a 1 entre estudiantes y profesores")
@RestController
@RequestMapping("/mensajes")
public class MensajesController {

    @Autowired
    private MensajeService mensajeService;

    @Operation(summary = "Listar conversaciones", description = "Devuelve la lista de conversaciones del usuario autenticado.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ConversacionDTO>>> listarConversaciones(Authentication authentication) {
        List<ConversacionDTO> conversaciones = mensajeService.listarConversaciones(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(conversaciones, "Conversaciones obtenidas"));
    }

    @Operation(summary = "Crear conversación", description = "Inicia una nueva conversación con otro usuario.")
    @PostMapping("/nuevo/{idUsuario}")
    @RateLimited(scope = "mensajes")
    public ResponseEntity<ApiResponse<ConversacionDTO>> crearConversacion(
            @PathVariable Integer idUsuario, Authentication authentication) {

        Integer idConversacion = mensajeService.crearConversacion(authentication.getName(), idUsuario);
        DetalleConversacionDTO detalle = mensajeService.obtenerDetalle(authentication.getName(), idConversacion);

        ConversacionDTO conversacion = ConversacionDTO.builder()
                .id(detalle.getId())
                .idUsuario(detalle.getIdUsuario())
                .nombre(detalle.getNombre())
                .rol(detalle.getSubtitulo())
                .foto(detalle.getFoto())
                .esProf(detalle.isEsProf())
                .preview("")
                .tiempo("")
                .enLinea(detalle.isEnLinea())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(conversacion, "Conversación creada"));
    }

    @Operation(summary = "Obtener conversación", description = "Devuelve el detalle de una conversación (mensajes incluidos).")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DetalleConversacionDTO>> obtenerConversacion(
            @PathVariable Integer id, Authentication authentication) {

        DetalleConversacionDTO detalle = mensajeService.obtenerDetalle(authentication.getName(), id);
        return ResponseEntity.ok(ApiResponse.success(detalle, "Conversación obtenida"));
    }

    @Operation(summary = "Enviar mensaje", description = "Envía un mensaje de texto en una conversación.")
    @PostMapping("/{id}")
    @RateLimited(scope = "mensajes")
    public ResponseEntity<ApiResponse<MensajeDTO>> enviarMensaje(
            @PathVariable Integer id,
            @Valid @RequestBody MensajeRequestDTO request,
            Authentication authentication) {

        MensajeDTO mensaje = mensajeService.enviarMensaje(authentication.getName(), id, request.getTexto());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(mensaje, "Mensaje enviado"));
    }

    @Operation(summary = "Enviar archivo adjunto", description = "Adjunta un archivo (max 20MB) a un mensaje.")
    @PostMapping(value = "/{id}/adjunto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RateLimited(scope = "mensajes")
    public ResponseEntity<ApiResponse<MensajeDTO>> enviarAdjunto(
            @PathVariable Integer id,
            @RequestParam(value = "texto", required = false) String texto,
            @RequestParam("archivo") MultipartFile archivo,
            Authentication authentication) {

        MensajeDTO mensaje = mensajeService.adjuntarArchivo(authentication.getName(), id, texto, archivo);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(mensaje, "Adjunto enviado"));
    }

    @GetMapping("/{id}/adjunto")
    @Operation(summary = "Descargar archivo adjunto", description = "Descarga el archivo adjunto de un mensaje.")
    public ResponseEntity<byte[]> descargarAdjunto(
            @PathVariable Integer id, Authentication authentication) {

        MensajeService.AdjuntoDescarga adjunto = mensajeService.obtenerAdjunto(authentication.getName(), id);

        String disposition = ArchivoSeguro.encabezadoDescargaSeguro(adjunto.getNombre());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .header(HttpHeaders.CONTENT_TYPE, adjunto.getTipo())
                .contentLength(adjunto.getContenido().length)
                .body(adjunto.getContenido());
    }

    @Operation(summary = "Marcar como leídos", description = "Marca todos los mensajes de una conversación como leídos.")
    @PostMapping("/{id}/leido")
    public ResponseEntity<ApiResponse<Void>> marcarLeidos(
            @PathVariable Integer id, Authentication authentication) {

        mensajeService.marcarLeidos(authentication.getName(), id);
        return ResponseEntity.ok(ApiResponse.success(null, "Mensajes marcados como leídos"));
    }

    @Operation(summary = "Indicar estado de escritura", description = "Emite evento SSE typing a los participantes de la conversación.")
    @PostMapping("/{id}/typing")
    public ResponseEntity<ApiResponse<Void>> indicarTyping(
            @PathVariable Integer id,
            @RequestBody(required = false) java.util.Map<String, Boolean> body,
            Authentication authentication) {

        boolean escribiendo = body != null && Boolean.TRUE.equals(body.get("escribiendo"));
        mensajeService.emitirTyping(id, authentication.getName(), escribiendo);
        return ResponseEntity.ok(ApiResponse.success(null, "Estado de escritura actualizado"));
    }

    @Operation(summary = "Stream SSE", description = "Suscripción a eventos en tiempo real de una conversación. " +
            "Reinicia con backoff exponencial. Requiere token JWT.")
    @GetMapping(value = "/{id}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamMensajes(@PathVariable Integer id, Authentication authentication) {
        return mensajeService.suscribirse(id, authentication.getName());
    }

}

