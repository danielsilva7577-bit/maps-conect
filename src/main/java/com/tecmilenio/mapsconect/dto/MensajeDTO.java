package com.tecmilenio.mapsconect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO con los datos de un mensaje (texto, tiempo, autor, adjunto, estado de lectura).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MensajeDTO {

    private Integer id;
    private String texto;
    private String tiempo;
    private String fechaEnvio;
    private boolean propio;
    private boolean leido;
    private String autorNombre;
    private Integer autorId;
    private String adjuntoNombre;
    private String adjuntoTipo;
    private Long adjuntoTamano;

}
