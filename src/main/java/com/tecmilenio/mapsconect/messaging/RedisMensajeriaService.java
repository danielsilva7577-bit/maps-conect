package com.tecmilenio.mapsconect.messaging;

import com.google.gson.Gson;
import com.tecmilenio.mapsconect.dto.MensajeDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * Implementación distribuida del bus de mensajería usando Redis Pub/Sub.
 *
 * <p>Permite que múltiples instancias de MAPS Connect sincronicen en tiempo real
 * chats, avisos y notificaciones push sin importar en qué servidor esté conectado el usuario.</p>
 */
@Service("redisMensajeriaService")
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "true")
public class RedisMensajeriaService implements MensajeriaDistribuidaService, MessageListener {

    private static final Logger log = LoggerFactory.getLogger(RedisMensajeriaService.class);
    public static final String CANAL_CLUSTER = "mapsconect:cluster:eventos";

    private final StringRedisTemplate redisTemplate;
    private final Gson gson = new Gson();
    private DespachadorEventosLocal despachador;

    public RedisMensajeriaService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        log.info("[Cluster] Bus distribuido de eventos activado mediante Redis Pub/Sub en canal '{}'", CANAL_CLUSTER);
    }

    @Autowired
    public void setDespachador(@Lazy DespachadorEventosLocal despachador) {
        this.despachador = despachador;
    }

    @Override
    public void publicarMensajeChat(Integer idConversacion, MensajeDTO mensaje, Integer idOtroUsuario) {
        EventoCluster evento = EventoCluster.builder()
                .tipo(EventoCluster.TipoEvento.MENSAJE_CHAT)
                .idConversacion(idConversacion)
                .idUsuarioDestino(idOtroUsuario)
                .payload(mensaje)
                .build();

        publicar(evento);
    }

    @Override
    public void publicarNotificacion(Integer idUsuario, Object payload) {
        EventoCluster evento = EventoCluster.builder()
                .tipo(EventoCluster.TipoEvento.NOTIFICACION_USUARIO)
                .idUsuarioDestino(idUsuario)
                .payload(payload)
                .build();

        publicar(evento);
    }

    @Override
    public void publicarTyping(Integer idConversacion, String emailEmisor, boolean escribiendo) {
        EventoCluster evento = EventoCluster.builder()
                .tipo(EventoCluster.TipoEvento.TYPING)
                .idConversacion(idConversacion)
                .emisorEmail(emailEmisor)
                .escribiendo(escribiendo)
                .build();

        publicar(evento);
    }

    @Override
    public boolean esDistribuido() {
        return true;
    }

    private void publicar(EventoCluster evento) {
        try {
            String json = gson.toJson(evento);
            redisTemplate.convertAndSend(CANAL_CLUSTER, json);
        } catch (Exception e) {
            log.error("[Cluster] Error publicando evento a Redis: {}", e.getMessage());
            // Fallback local defensivo en caso de caída temporal de Redis
            if (despachador != null) {
                despacharLocalmente(evento);
            }
        }
    }

    /**
     * Callback invocado cuando Redis entrega un mensaje publicado por CUALQUIER
     * nodo del cluster (incluido el propio o réplicas).
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String json = new String(message.getBody(), StandardCharsets.UTF_8);
            EventoCluster evento = gson.fromJson(json, EventoCluster.class);
            despacharLocalmente(evento);
        } catch (Exception e) {
            log.error("[Cluster] Error procesando mensaje de Redis Pub/Sub: {}", e.getMessage());
        }
    }

    private void despacharLocalmente(EventoCluster evento) {
        if (despachador == null || evento == null || evento.getTipo() == null) {
            return;
        }

        switch (evento.getTipo()) {
            case MENSAJE_CHAT -> {
                // Reconstruir MensajeDTO si vino como LinkedTreeMap desde Gson
                MensajeDTO dto;
                if (evento.getPayload() instanceof MensajeDTO m) {
                    dto = m;
                } else {
                    dto = gson.fromJson(gson.toJson(evento.getPayload()), MensajeDTO.class);
                }
                despachador.despacharMensajeLocal(evento.getIdConversacion(), dto);
                if (evento.getIdUsuarioDestino() != null) {
                    despachador.despacharNotificacionDestinatarioLocal(
                            evento.getIdUsuarioDestino(), evento.getIdConversacion(), dto);
                }
            }
            case NOTIFICACION_USUARIO -> {
                despachador.despacharNotificacionUsuarioLocal(evento.getIdUsuarioDestino(), evento.getPayload());
            }
            case TYPING -> {
                despachador.despacharTypingLocal(
                        evento.getIdConversacion(),
                        evento.getEmisorEmail(),
                        Boolean.TRUE.equals(evento.getEscribiendo()));
            }
        }
    }

}
