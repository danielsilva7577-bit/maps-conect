package com.tecmilenio.mapsconect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Conversacion de chat privado entre dos usuarios.
 */
@Entity
@Table(name = "conversaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_conversacion")
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario_1", nullable = false)
    private Usuario usuario1;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario_2", nullable = false)
    private Usuario usuario2;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

}
