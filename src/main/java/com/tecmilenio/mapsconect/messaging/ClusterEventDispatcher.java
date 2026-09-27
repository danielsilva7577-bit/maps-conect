package com.tecmilenio.mapsconect.messaging;

import com.tecmilenio.mapsconect.dto.MensajeDTO;
import com.tecmilenio.mapsconect.service.MensajeService;
import com.tecmilenio.mapsconect.service.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Despachador centralizado que entrega eventos distribuidos a las conexiones SSE
 * activas en esta instancia local del backend.
 */
@Component
public class ClusterEventDispatcher implements DespachadorEventosLocal {

    private final MensajeService mensajeService;
    private final NotificacionService notificacionService;

    @Autowired
    public ClusterEventDispatcher(@Lazy MensajeService mensajeService,
                                  @Lazy NotificacionService notificacionService) {
        this.mensajeService = mensajeService;
        this.notificacionService = notificacionService;
    }

    @Override
    public void despacharMensajeLocal(Integer idConversacion, MensajeDTO mensaje) {
        if (mensajeService != null) {
            mensajeService.despacharMensajeLocal(idConversacion, mensaje);
        }
    }

    @Override
    public void despacharNotificacionDestinatarioLocal(Integer idUsuario, Integer idConversacion, MensajeDTO mensaje) {
        if (mensajeService != null) {
            mensajeService.despacharNotificacionDestinatarioLocal(idUsuario, idConversacion, mensaje);
        }
    }

    @Override
    public void despacharTypingLocal(Integer idConversacion, String email, boolean escribiendo) {
        if (mensajeService != null) {
            mensajeService.despacharTypingLocal(idConversacion, email, escribiendo);
        }
    }

    @Override
    public void despacharNotificacionUsuarioLocal(Integer idUsuario, Object payload) {
        if (notificacionService != null) {
            notificacionService.despacharNotificacionLocal(idUsuario, payload);
        }
    }

}
