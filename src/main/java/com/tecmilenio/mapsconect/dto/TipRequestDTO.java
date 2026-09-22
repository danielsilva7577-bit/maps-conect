package com.tecmilenio.mapsconect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO para crear un nuevo tip.
 */
@Data
public class TipRequestDTO {

    @NotBlank(message = "El contenido del tip es obligatorio")
    @Size(max = 4000, message = "El tip no puede exceder 4000 caracteres")
    private String contenido;

    private Integer idMateria;

}
