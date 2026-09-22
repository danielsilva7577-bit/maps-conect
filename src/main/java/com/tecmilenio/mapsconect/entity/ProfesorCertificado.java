package com.tecmilenio.mapsconect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "profesores_certificados")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfesorCertificado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_profesor_certificado")
    private Integer id;

    @Column(name = "id_profesor", nullable = false)
    private Integer idProfesor;

    @Column(name = "id_certificado", nullable = false)
    private Integer idCertificado;

}