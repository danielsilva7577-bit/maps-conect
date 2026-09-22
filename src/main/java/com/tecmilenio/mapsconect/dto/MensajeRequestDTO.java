package com.tecmilenio.mapsconect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO para enviar un mensaje de texto a una conversacion.
 */
@Data
public class MensajeRequestDTO {

    @NotBlank(message = "El mensaje no puede estar vacío")
    @Size(max = 4000, message = "El mensaje no puede exceder 4000 caracteres")
    private String texto;

}
