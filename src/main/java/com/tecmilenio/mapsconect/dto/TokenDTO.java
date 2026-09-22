package com.tecmilenio.mapsconect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que contiene el token JWT y datos del usuario autenticado.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenDTO {

    private String token;
    @Builder.Default
    private String tipo = "Bearer";
    private Long expiresIn;
    private UsuarioDTO usuario;

}

