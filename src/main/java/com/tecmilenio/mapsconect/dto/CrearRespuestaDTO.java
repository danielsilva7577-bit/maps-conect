package com.tecmilenio.mapsconect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearRespuestaDTO {

    @NotBlank(message = "El contenido de la respuesta no puede estar vacío")
    @Size(max = 5000, message = "La respuesta no puede exceder 5000 caracteres")
    private String contenido;

}
