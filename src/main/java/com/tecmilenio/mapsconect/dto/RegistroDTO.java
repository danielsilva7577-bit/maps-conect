package com.tecmilenio.mapsconect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para la peticion de registro de nuevo usuario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroDTO {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    @Pattern(regexp = "^[^@\\s]+@([a-zA-Z0-9.-]+\\.)?tecmilenio\\.(mx|edu\\.mx)$",
            message = "Usa tu correo institucional del Tecmilenio (@tecmilenio.mx)")
    private String email;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
    private String apellido;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(max = 100, message = "La contraseña no puede superar 100 caracteres")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
            message = "La contraseña debe tener al menos 8 caracteres y combinar mayúscula, minúscula y número")
    private String contrasena;

    private String rol;

    private String matricula;

    private String numeroNomina;

}
