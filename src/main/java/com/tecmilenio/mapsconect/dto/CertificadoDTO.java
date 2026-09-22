package com.tecmilenio.mapsconect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO con los datos de un certificado y sus materias.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificadoDTO {

    private Integer id;
    private String nombre;
    private String descripcion;

    @Builder.Default
    private List<MateriaSimpleDTO> materias = new ArrayList<>();

}
