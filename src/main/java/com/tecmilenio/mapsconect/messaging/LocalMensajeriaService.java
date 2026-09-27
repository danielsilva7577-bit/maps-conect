package com.tecmilenio.mapsconect.messaging;

import com.tecmilenio.mapsconect.dto.MensajeDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * Implementación local en memoria del bus de eventos.
 *
 * <p>Usada por defecto cuando la aplicación se ejecuta en una sola instancia
 * (sin cluster Redis). Despacha directamente a los emisores locales.</p>
 */
@Service("localMensajeriaService")
public class LocalMensajeriaService implements MensajeriaDistribuidaService {

    private static final Logger log = LoggerFactory.getLogger(LocalMensajeriaService.class);

    private DespachadorEventosLocal despachador;

    @Autowired
    public void setDespachador(@Lazy DespachadorEventosLocal despachador) {
        this.despachador = despachador;
    }

    @Override
    public void publicarMensajeChat(Integer idConversacion, MensajeDTO mensaje, Integer idOtroUsuario) {
        if (despachador != null) {
            despachador.despacharMensajeLocal(idConversacion, mensaje);
            if (idOtroUsuario != null) {
                despachador.despacharNotificacionDestinatarioLocal(idOtroUsuario, idConversacion, mensaje);
            }
        }
    }

    @Override
    public void publicarNotificacion(Integer idUsuario, Object payload) {
        if (despachador != null) {
            despachador.despacharNotificacionUsuarioLocal(idUsuario, payload);
        }
    }

    @Override
    public void publicarTyping(Integer idConversacion, String emailEmisor, boolean escribiendo) {
        if (despachador != null) {
            despachador.despacharTypingLocal(idConversacion, emailEmisor, escribiendo);
        }
    }

    @Override
    public boolean esDistribuido() {
        return false;
    }

}
