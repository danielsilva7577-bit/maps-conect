package com.tecmilenio.mapsconect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO con los datos de una conversacion (id, nombre del otro, foto, preview, tiempo).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversacionDTO {

    private Integer id;
    private Integer idUsuario;
    private String nombre;
    private String rol;
    private String foto;
    private boolean esProf;
    private String preview;
    private String tiempo;
    private String fechaUltimoMensaje;
    private boolean enLinea;

}
