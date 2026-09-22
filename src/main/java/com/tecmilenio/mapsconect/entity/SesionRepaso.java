package com.tecmilenio.mapsconect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "sesiones_repaso")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SesionRepaso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sesion")
    private Integer id;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "descripcion", columnDefinition = "text")
    private String descripcion;

    @Column(name = "materia", nullable = false, length = 200)
    private String materia;

    @Column(name = "modalidad", nullable = false, length = 50)
    private String modalidad;

    @Column(name = "ubicacion", length = 200)
    private String ubicacion;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "duracion_min", nullable = false)
    private Integer duracionMin;

    @Column(name = "cupo_max", nullable = false)
    private Integer cupoMax;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "organizador_id", nullable = false)
    private Integer organizadorId;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

}