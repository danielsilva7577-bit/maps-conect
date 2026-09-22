package com.tecmilenio.mapsconect.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para cambiar el semestre actual del estudiante.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SemestreRequestDTO {

    @NotNull(message = "El semestre es obligatorio")
    @Min(value = 1, message = "El semestre debe estar entre 1 y 12")
    @Max(value = 12, message = "El semestre debe estar entre 1 y 12")
    private Integer semestre;

}
