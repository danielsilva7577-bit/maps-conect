package com.tecmilenio.mapsconect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO con los datos de una respuesta al foro.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RespuestaForoDTO {

    private Integer id;
    private Integer idPublicacion;
    private String contenido;
    private String autor;
    private Integer autorId;
    private String autorFoto;
    private String fecha;
    private boolean esSolucion;
    private boolean esVerificadaDocente;

}
