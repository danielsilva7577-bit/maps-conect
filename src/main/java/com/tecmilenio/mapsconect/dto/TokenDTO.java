package com.tecmilenio.mapsconect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que contiene el access token JWT, el refresh token opaco
 * y datos del usuario autenticado.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenDTO {

    /** Access token JWT (corta duración: 24h prod, 7d dev). */
    private String token;

    /** Tipo de token: siempre "Bearer". */
    @Builder.Default
    private String tipo = "Bearer";

    /** Segundos hasta la expiración del access token. */
    private Long expiresIn;

    /** Refresh token opaco (UUID). Duración: 30 días. */
    private String refreshToken;

    /** Segundos hasta la expiración del refresh token (30 días = 2592000). */
    @Builder.Default
    private long refreshExpiresIn = 2592000L;

    private UsuarioDTO usuario;

}
