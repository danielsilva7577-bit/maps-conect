package com.tecmilenio.mapsconect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "profesores_materias")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfesorMateria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_profesor_materia")
    private Integer id;

    @Column(name = "id_profesor", nullable = false)
    private Integer idProfesor;

    @Column(name = "id_materia", nullable = false)
    private Integer idMateria;

    @Column(name = "ciclo_academico", nullable = false, length = 20)
    private String cicloAcademico;

}