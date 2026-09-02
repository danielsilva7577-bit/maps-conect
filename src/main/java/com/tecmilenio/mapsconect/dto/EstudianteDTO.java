package com.tecmilenio.mapsconect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstudianteDTO {

    private Integer id;
    private Integer idUsuario;
    private String nombreCompleto;
    private String email;
    private Integer idCarrera;
    private String matricula;
    private Integer semestreActual;
    private String propositoVida;
    private Integer puntosReputacion;

}
