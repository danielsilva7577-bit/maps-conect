package com.tecmilenio.mapsconect.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CrearSesionRepasoDTO {

    @NotBlank(message = "El título de la sesión es obligatorio")
    @Size(max = 200, message = "El título no puede superar los 200 caracteres")
    private String titulo;

    @Size(max = 2000, message = "La descripción no puede superar los 2000 caracteres")
    private String descripcion;

    @NotBlank(message = "Indica la materia de la sesión")
    @Size(max = 200, message = "La materia no puede superar los 200 caracteres")
    private String materia;

    @Size(max = 50)
    private String modalidad;

    @Size(max = 200)
    private String ubicacion;

    @NotNull(message = "Selecciona la fecha de la sesión")
    private String fecha;

    @NotBlank(message = "Indica la hora de inicio")
    private String horaInicio;

    @Min(value = 15, message = "La duración mínima es de 15 minutos")
    @Max(value = 480, message = "La duración máxima es de 480 minutos")
    private Integer duracionMin;

    @Min(value = 1, message = "El cupo mínimo es de 1 persona")
    @Max(value = 300, message = "El cupo máximo es de 300 personas")
    private Integer cupoMax;

}