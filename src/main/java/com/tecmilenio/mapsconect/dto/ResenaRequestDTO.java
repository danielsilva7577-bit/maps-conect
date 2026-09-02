package com.tecmilenio.mapsconect.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResenaRequestDTO {

    @NotNull(message = "La empresa es obligatoria")
    private Integer idEmpresa;

    @NotNull(message = "La calificación es obligatoria")
    @Min(value = 1, message = "La calificación debe estar entre 1 y 5")
    @Max(value = 5, message = "La calificación debe estar entre 1 y 5")
    private Integer calificacion;

    private String proyectoRealizado;

    private String recomendaciones;

}
