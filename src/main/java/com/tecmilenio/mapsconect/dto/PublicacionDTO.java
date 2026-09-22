package com.tecmilenio.mapsconect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO con los datos de una publicacion del foro.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicacionDTO {

    private Integer id;
    private String titulo;
    private String descripcion;
    private Integer idMateria;
    private String materia;
    private String autor;
    private String tiempo;
    private int votos;
    private int respuestas;
    private boolean resuelto;
    private String solucion;
    private Integer autorId;
    private String autorFoto;
    private boolean siguiendo;
    private String semestre;

}

