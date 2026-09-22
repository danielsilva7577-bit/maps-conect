package com.tecmilenio.mapsconect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para actualizar la foto de perfil (data URI).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FotoRequestDTO {

    private String foto;

}
