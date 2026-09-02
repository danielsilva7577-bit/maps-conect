package com.tecmilenio.mapsconect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpresaDTO {

    private Integer id;
    private String nombre;
    private String sector;
    private String descripcion;
    private Boolean activa;
    private Double calificacion;
    private Long totalResenas;
    private ExperienciaDTO experienciaDestacada;

}
