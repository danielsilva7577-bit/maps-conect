package com.tecmilenio.mapsconect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    @ManyToOne
    @JoinColumn(name = "id_estudiante", nullable = false)
    private Estudiante estudiante;

    @ManyToOne
    @JoinColumn(name = "id_empresa", nullable = false)
    private EmpresaVinculada empresa;

    @Column(nullable = false)
    private Integer calificacion;

    @Column(name = "proyecto_realizado", columnDefinition = "TEXT")
    private String proyectoRealizado;

    @Column(columnDefinition = "TEXT")
    private String recomendaciones;

    @Builder.Default
    @Column(name = "fecha_resena", nullable = false)
    private LocalDateTime fechaResena = LocalDateTime.now();

}
