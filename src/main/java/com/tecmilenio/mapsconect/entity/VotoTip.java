package com.tecmilenio.mapsconect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Voto positivo (upvote) de un usuario sobre un tip académico.
 */
@Entity
@Table(name = "votos_tips")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VotoTip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_voto")
    private Integer id;

    @Column(name = "id_tip", nullable = false)
    private Integer idTip;

    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    @Column(name = "fecha_voto", nullable = false)
    private LocalDateTime fechaVoto;

}
