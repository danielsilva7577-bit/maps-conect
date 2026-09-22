package com.tecmilenio.mapsconect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Tip o consejo académico breve asociado a una materia.
 */
@Entity
@Table(name = "tips_academicos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipAcademico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tip")
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario autor;

    @Column(name = "id_materia", nullable = false)
    private Integer idMateria;

    @Column(name = "contenido", nullable = false, columnDefinition = "text")
    private String contenido;

    @Column(name = "fecha_publicacion", nullable = false)
    private LocalDateTime fechaPublicacion;

    @Column(name = "total_votos", nullable = false)
    private Integer totalVotos;

}
