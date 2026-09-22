package com.tecmilenio.mapsconect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Relacion de seguimiento entre dos usuarios (seguidor y seguido).
 */
@Entity
@Table(name = "seguimientos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seguimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_seguimiento")
    private Integer id;

    @Column(name = "id_seguidor", nullable = false)
    private Integer idSeguidor;

    @Column(name = "id_seguido", nullable = false)
    private Integer idSeguido;

    @Builder.Default
    @Column(name = "fecha_seguimiento", nullable = false)
    private LocalDateTime fechaSeguimiento = LocalDateTime.now();

}
