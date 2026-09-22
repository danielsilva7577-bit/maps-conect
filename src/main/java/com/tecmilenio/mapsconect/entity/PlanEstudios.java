package com.tecmilenio.mapsconect.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "plan_estudios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanEstudios {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_plan_estudios")
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_carrera", nullable = false)
    private Carrera carrera;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_materia", nullable = false)
    private Materia materia;

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(name = "semestre_sugerido", nullable = false)
    private Integer semestre;

}