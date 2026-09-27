package com.tecmilenio.mapsconect.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO para solicitar un nuevo access token usando un refresh token.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshRequestDTO {

    @NotBlank(message = "El refresh token es obligatorio")
    private String refreshToken;
}
