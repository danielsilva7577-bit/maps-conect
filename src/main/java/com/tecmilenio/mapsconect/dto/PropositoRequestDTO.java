package com.tecmilenio.mapsconect.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropositoRequestDTO {

    @Size(max = 1000, message = "El propósito de vida no puede superar los 1000 caracteres")
    private String proposito;
}
