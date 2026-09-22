package com.tecmilenio.mapsconect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Certificado academico: trayectoria de aprendizaje opcional que agrupa materias.
 */
@Entity
@Table(name = "certificados")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certificado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_certificado")
    private Integer id;

    @Column(name = "nombre_certificado", nullable = false, unique = true, length = 150)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "text")
    private String descripcion;

}
