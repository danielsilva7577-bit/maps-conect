package com.tecmilenio.mapsconect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "estudiantes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Estudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estudiante")
    private Integer id;

    @OneToOne
    @JoinColumn(name = "id_usuario", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "id_carrera")
    private Integer idCarrera;

    @Column(nullable = false, length = 20)
    private String matricula;

    @Column(name = "semestre_actual", nullable = false)
    private Integer semestreActual;

    @Column(name = "proposito_vida", columnDefinition = "TEXT")
    private String propositoVida;

    @Builder.Default
    @Column(name = "puntos_reputacion", nullable = false)
    private Integer puntosReputacion = 0;

}
