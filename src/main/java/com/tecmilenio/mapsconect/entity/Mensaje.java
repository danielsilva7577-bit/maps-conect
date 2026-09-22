package com.tecmilenio.mapsconect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Mensaje dentro de una conversacion. Puede incluir texto e incluso un archivo adjunto.
 */
@Entity
@Table(name = "mensajes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mensaje")
    private Integer id;

    @Column(name = "id_conversacion", nullable = false)
    private Integer idConversacion;

    @Column(name = "id_emisor", nullable = false)
    private Integer idEmisor;

    @Column(name = "contenido", nullable = false, columnDefinition = "text")
    private String contenido;

    @Column(name = "adjunto_nombre")
    private String adjuntoNombre;

    @Column(name = "adjunto_tipo")
    private String adjuntoTipo;

    @Column(name = "adjunto_tamano")
    private Long adjuntoTamano;

    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fechaEnvio;

    @Column(name = "leido", nullable = false)
    private Boolean leido;

}
