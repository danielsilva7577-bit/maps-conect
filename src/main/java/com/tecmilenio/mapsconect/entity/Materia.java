package com.tecmilenio.mapsconect.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "materias")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Materia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_materia")
    private Integer id;

    @Column(name = "nombre_materia", nullable = false, length = 150)
    private String nombre;

    @Column(name = "clave_materia", nullable = false, unique = true, length = 20)
    private String clave;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "tipo_materia", nullable = false)
    private String tipo;

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(name = "creditos")
    private Integer creditos;

}