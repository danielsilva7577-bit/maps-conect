package com.tecmilenio.mapsconect.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstudianteRequestDTO {

    private Integer idCarrera;

    @NotBlank(message = "La matrícula es obligatoria")
    @jakarta.validation.constraints.Size(max = 20, message = "La matrícula no puede exceder 20 caracteres")
    private String matricula;

    @NotNull(message = "El semestre actual es obligatorio")
    @Min(value = 1, message = "El semestre debe estar entre 1 y 12")
    @Max(value = 12, message = "El semestre debe estar entre 1 y 12")
    private Integer semestreActual;

    private String propositoVida;

}
