package com.tecmilenio.mapsconect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Inscripcion de un estudiante a una materia del semestre actual.
 */
@Entity
@Table(name = "estudiantes_materias")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstudianteMateria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estudiante_materia")
    private Integer id;

    @Column(name = "id_estudiante", nullable = false)
    private Integer idEstudiante;

    @Column(name = "id_materia", nullable = false)
    private Integer idMateria;

    @Column(name = "fecha_seleccion", nullable = false, insertable = false, updatable = false)
    private LocalDateTime fechaSeleccion;

}

