package com.tecmilenio.mapsconect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para cambiar la contrasena: actual, nueva y confirmacion.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CambiarContrasenaDTO {

    @NotBlank(message = "La contraseña actual es obligatoria")
    private String contrasenaActual;

    @NotBlank(message = "La contraseña nueva es obligatoria")
    @Size(max = 100, message = "La contraseña no puede superar 100 caracteres")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
            message = "La contraseña debe tener al menos 8 caracteres y combinar mayúscula, minúscula y número")
    private String contrasenaNueva;

}
