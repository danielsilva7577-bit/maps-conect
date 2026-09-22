package com.tecmilenio.mapsconect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO con los datos de un recurso academico.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecursoDTO {

    private Integer id;
    private String titulo;
    private String descripcion;
    private String materia;
    private String autor;
    private String tipo;
    private String url;
    private int descargas;
    private String tiempo;
    private boolean interno;
    private String adjuntoNombre;
    private Long adjuntoTamano;

}

