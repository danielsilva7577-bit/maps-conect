package com.tecmilenio.mapsconect.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnboardingEstudianteDTO {

    @NotBlank(message = "La matrícula es obligatoria")
    private String matricula;

    @NotNull(message = "La carrera es obligatoria")
    private Integer idCarrera;

    @NotNull(message = "El semestre es obligatorio")
    @Min(value = 1, message = "El semestre debe estar entre 1 y 12")
    @Max(value = 12, message = "El semestre debe estar entre 1 y 12")
    private Integer semestre;

    @Builder.Default
    private List<Integer> materias = new ArrayList<>();

}