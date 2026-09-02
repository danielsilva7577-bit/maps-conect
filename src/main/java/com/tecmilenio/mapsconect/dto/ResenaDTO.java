package com.tecmilenio.mapsconect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResenaDTO {

    private Integer id;
    private Integer idEmpresa;
    private Integer idEstudiante;
    private String nombreEstudiante;
    private Integer calificacion;
    private String proyectoRealizado;
    private String recomendaciones;
    private LocalDateTime fechaResena;

}
