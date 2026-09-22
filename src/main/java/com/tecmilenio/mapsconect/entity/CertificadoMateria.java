package com.tecmilenio.mapsconect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Relacion entre un certificado y una materia que lo componen.
 */
@Entity
@Table(name = "certificado_materias")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificadoMateria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_certificado_materia")
    private Integer id;

    @Column(name = "id_certificado", nullable = false)
    private Integer idCertificado;

    @Column(name = "id_materia", nullable = false)
    private Integer idMateria;

}
