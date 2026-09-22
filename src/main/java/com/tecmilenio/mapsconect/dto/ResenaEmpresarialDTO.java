package com.tecmilenio.mapsconect.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResenaEmpresarialDTO {

    @NotNull(message = "La calificación es obligatoria")
    @Min(value = 1, message = "La calificación mínima es 1 estrella")
    @Max(value = 5, message = "La calificación máxima es 5 estrellas")
    private Integer calificacion;

    @Size(max = 2000, message = "El proyecto no puede superar los 2000 caracteres")
    private String proyectoDesarrollado;

    @Size(max = 2000, message = "Los aprendizajes no pueden superar los 2000 caracteres")
    private String aprendizajes;

    @Size(max = 2000, message = "Las recomendaciones no pueden superar los 2000 caracteres")
    private String recomendaciones;

}