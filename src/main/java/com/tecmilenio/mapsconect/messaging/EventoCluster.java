package com.tecmilenio.mapsconect.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Evento serializable transmitido a través del bus de mensajería distribuida
 * (Redis Pub/Sub) para sincronizar SSE entre múltiples instancias de la aplicación.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoCluster implements Serializable {

    private TipoEvento tipo;
    private Integer idConversacion;
    private Integer idUsuarioDestino;
    private Object payload;
    private String emisorEmail;
    private Boolean escribiendo;

    public enum TipoEvento {
        MENSAJE_CHAT,
        NOTIFICACION_USUARIO,
        TYPING
    }

}
