package com.tecmilenio.mapsconect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear un nuevo recurso academico.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearRecursoDTO {

    @NotBlank(message = "El título del apunte es obligatorio")
    @Size(max = 200, message = "El título no puede superar los 200 caracteres")
    private String titulo;

    @Size(max = 2000, message = "La descripción no puede superar los 2000 caracteres")
    private String descripcion;

    @NotNull(message = "Selecciona una materia para el apunte")
    private Integer idMateria;

    @NotBlank(message = "Agrega la URL o enlace de descarga del apunte")
    @Size(max = 500, message = "La URL no puede superar los 500 caracteres")
    private String url;

    @Size(max = 50)
    private String tipo;

}
