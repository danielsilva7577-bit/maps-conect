package com.tecmilenio.mapsconect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear una nueva publicacion en el foro.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearPublicacionDTO {

    @NotBlank(message = "El título de la duda es obligatorio")
    @Size(max = 200, message = "El título no puede superar 200 caracteres")
    private String titulo;

    @NotBlank(message = "El contenido de la duda es obligatorio")
    @Size(max = 4000, message = "El contenido no puede superar 4000 caracteres")
    private String contenido;

    private Integer idMateria;

    private boolean ignorarDuplicado;

}
