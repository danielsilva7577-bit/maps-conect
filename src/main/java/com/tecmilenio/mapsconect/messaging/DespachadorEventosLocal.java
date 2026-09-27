package com.tecmilenio.mapsconect.messaging;

import com.tecmilenio.mapsconect.dto.MensajeDTO;

/**
 * Receptor de eventos locales para que los servicios que mantienen conexiones SSE
 * (como chat y notificaciones) despachen a los clientes conectados en la instancia actual.
 */
public interface DespachadorEventosLocal {

    void despacharMensajeLocal(Integer idConversacion, MensajeDTO mensaje);

    void despacharNotificacionDestinatarioLocal(Integer idUsuario, Integer idConversacion, MensajeDTO mensaje);

    void despacharTypingLocal(Integer idConversacion, String email, boolean escribiendo);

    void despacharNotificacionUsuarioLocal(Integer idUsuario, Object payload);

}
