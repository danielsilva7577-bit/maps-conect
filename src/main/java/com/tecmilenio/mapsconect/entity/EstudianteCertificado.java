package com.tecmilenio.mapsconect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Asignacion de un certificado a un estudiante, con fecha de seleccion.
 */
@Entity
@Table(name = "estudiante_certificados")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstudianteCertificado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estudiante_certificado")
    private Integer id;

    @Column(name = "id_estudiante", nullable = false)
    private Integer idEstudiante;

    @Column(name = "id_certificado", nullable = false)
    private Integer idCertificado;

    @Column(name = "fecha_seleccion", nullable = false)
    private LocalDateTime fechaSeleccion;

}
