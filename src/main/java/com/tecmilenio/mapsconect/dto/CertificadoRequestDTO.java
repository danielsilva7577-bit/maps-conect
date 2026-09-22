package com.tecmilenio.mapsconect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO para crear un nuevo certificado.
 */
@Data
public class CertificadoRequestDTO {

    @NotBlank(message = "El nombre del certificado es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    private String nombre;

    @Size(max = 4000, message = "La descripción no puede exceder 4000 caracteres")
    private String descripcion;

}
