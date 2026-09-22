package com.tecmilenio.mapsconect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "carreras")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Carrera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carrera")
    private Integer id;

    @Column(name = "nombre_carrera", nullable = false, unique = true, length = 120)
    private String nombre;

    @Column(name = "clave_carrera", nullable = false, unique = true, length = 20)
    private String clave;

    @Column(name = "activa", nullable = false)
    private Boolean activa;

}