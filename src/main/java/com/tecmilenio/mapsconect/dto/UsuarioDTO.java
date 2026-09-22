package com.tecmilenio.mapsconect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO con los datos publicos del usuario (id, email, nombre, rol, foto).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioDTO {

    private Integer id;
    private String email;
    private String nombre;
    private String rol;
    private Boolean activo;
    private String foto;

}

