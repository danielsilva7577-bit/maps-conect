package com.tecmilenio.mapsconect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Empresa vinculada a la institucion para el Semestre Empresarial.
 */
@Entity
@Table(name = "empresas_vinculadas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpresaVinculada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empresa")
    private Integer id;

    @Column(name = "nombre_empresa", nullable = false, unique = true, length = 200)
    private String nombre;

    @Column(name = "sector", length = 100)
    private String sector;

    @Column(name = "sitio_web", length = 255)
    private String sitioWeb;

    @Column(name = "descripcion", columnDefinition = "text")
    private String descripcion;

    @Column(name = "carreras_afines", columnDefinition = "text")
    private String carrerasAfines;

}

