package com.tecmilenio.mapsconect.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
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

    @Column(name = "id_usuario", nullable = false, unique = true)
    private Integer idUsuario;

    @Column(name = "id_carrera", nullable = false)
    private Integer idCarrera;

    @Column(name = "matricula", nullable = false, unique = true, length = 20)
    private String matricula;

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(name = "semestre_actual", nullable = false)
    private Integer semestreActual;

    @Column(name = "proposito_vida", columnDefinition = "text")
    private String propositoVida;

}