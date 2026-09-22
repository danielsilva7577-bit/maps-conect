package com.tecmilenio.mapsconect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SesionRepasoDTO {

    private Integer id;
    private String titulo;
    private String descripcion;
    private String materia;
    private String modalidad;
    private String ubicacion;
    private String fecha;
    private String horaInicio;
    private int duracionMin;
    private int cupoMax;
    private String estado;
    private String organizador;
    private Integer organizadorId;
    private long inscritos;
    private boolean inscrito;
    private boolean organizadorYo;

}