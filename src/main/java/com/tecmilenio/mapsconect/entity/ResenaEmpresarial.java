package com.tecmilenio.mapsconect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Resena de experiencia de un estudiante en una empresa del Semestre Empresarial.
 */
@Entity
@Table(name = "resenas_empresarial")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResenaEmpresarial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_resena")
    private Integer id;

    @Column(name = "id_estudiante", nullable = false)
    private Integer idEstudiante;

    @Column(name = "id_empresa", nullable = false)
    private Integer idEmpresa;

    @Column(name = "calificacion", nullable = false)
    @JdbcTypeCode(SqlTypes.TINYINT)
    private Integer calificacion;

    @Column(name = "proyecto_desarrollado", columnDefinition = "text")
    private String proyectoDesarrollado;

    @Column(name = "aprendizajes", columnDefinition = "text")
    private String aprendizajes;

    @Column(name = "recomendaciones", columnDefinition = "text")
    private String recomendaciones;

    @Column(name = "fecha_resena", nullable = false)
    private LocalDateTime fechaResena;

}

