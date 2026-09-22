package com.tecmilenio.mapsconect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO con los datos de un tip academico.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipDTO {

    private Integer id;
    private String titulo;
    private String texto;
    private Integer idMateria;
    private String materia;
    private Integer autorId;
    private String autor;
    private boolean verificado;
    private int votos;
    private String fechaPublicacion;

}
