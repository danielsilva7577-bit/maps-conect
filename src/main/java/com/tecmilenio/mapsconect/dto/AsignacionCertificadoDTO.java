package com.tecmilenio.mapsconect.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO para asignar un certificado a un estudiante.
 */
@Data
public class AsignacionCertificadoDTO {

    @NotNull(message = "El id del estudiante es obligatorio")
    private Integer idEstudiante;

    @NotNull(message = "El id del certificado es obligatorio")
    private Integer idCertificado;

}
