package com.tecmilenio.mapsconect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO con el detalle completo de una conversacion (datos del otro usuario y mensajes).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleConversacionDTO {

    private Integer id;
    private Integer idUsuario;
    private String nombre;
    private String subtitulo;
    private String foto;
    private boolean esProf;
    private boolean enLinea;

    @Builder.Default
    private List<MensajeDTO> mensajes = new ArrayList<>();

}
