package com.tecmilenio.mapsconect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO con los datos de una pregunta duplicada (publicacion, similitud, respuestas, tips, recursos).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DuplicadoDTO {

    private PublicacionDTO publicacion;
    private double similitud;
    private List<RespuestaForoDTO> respuestas;
    private List<TipDTO> tips;
    private List<RecursoDTO> recursos;

}
