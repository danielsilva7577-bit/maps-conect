package com.tecmilenio.mapsconect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO con los datos de una empresa vinculada.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpresaVinculadaDTO {

    private Integer id;
    private String nombre;
    private String ciudad;
    private String estado;
    private String modalidad;
    private String sector;
    private String sitioWeb;
    private String descripcion;
    private Double calificacion;
    private long totalResenas;
    private boolean convenioActivo;

    @Builder.Default
    private List<String> tecnologias = new ArrayList<>();

}

