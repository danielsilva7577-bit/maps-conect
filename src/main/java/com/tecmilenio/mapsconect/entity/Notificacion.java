package com.tecmilenio.mapsconect.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Notificacion persistida (foro, sesiones, asesorias, etc.) para el centro de notificaciones.
 */
@Entity
@Table(name = "notificaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion")
    private Integer id;

    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    @Column(name = "tipo", nullable = false, length = 30)
    private String tipo;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "preview", length = 255)
    private String preview;

    @Column(name = "enlace", length = 500)
    private String enlace;

    @Column(name = "leida", nullable = false)
    private Boolean leida;

    @Column(name = "id_origen")
    private Integer idOrigen;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

}

