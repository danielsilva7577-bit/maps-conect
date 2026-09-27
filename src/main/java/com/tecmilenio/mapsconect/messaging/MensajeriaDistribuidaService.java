package com.tecmilenio.mapsconect.messaging;

import com.tecmilenio.mapsconect.dto.MensajeDTO;

/**
 * Contrato del bus de mensajería distribuida para sincronización en tiempo real.
 *
 * <p>En un entorno multi-servidor (horizontal), publica los eventos al cluster
 * (vía Redis Pub/Sub) para que todos los nodos activos reenvíen las actualizaciones
 * a sus respectivos clientes SSE conectados.</p>
 */
public interface MensajeriaDistribuidaService {

    /**
     * Publica un nuevo mensaje de chat hacia el cluster.
     *
     * @param idConversacion ID de la conversación
     * @param mensaje        DTO del mensaje enviado
     * @param idOtroUsuario  ID del destinatario (para alerta en campana/resumen)
     */
    void publicarMensajeChat(Integer idConversacion, MensajeDTO mensaje, Integer idOtroUsuario);

    /**
     * Publica una notificación push de usuario hacia el cluster.
     *
     * @param idUsuario ID del usuario receptor
     * @param payload   datos de la notificación
     */
    void publicarNotificacion(Integer idUsuario, Object payload);

    /**
     * Publica el indicador de "escribiendo..." en una conversación hacia el cluster.
     *
     * @param idConversacion ID de la conversación
     * @param emailEmisor    email del usuario que escribe
     * @param escribiendo    true si escribe, false si terminó
     */
    void publicarTyping(Integer idConversacion, String emailEmisor, boolean escribiendo);

    /**
     * Indica si el bus distribuido (Redis) está activo o si corre en modo local.
     *
     * @return true si corre en modo cluster distribuido
     */
    boolean esDistribuido();

}
