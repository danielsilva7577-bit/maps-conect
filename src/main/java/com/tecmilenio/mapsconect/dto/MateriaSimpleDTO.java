package com.tecmilenio.mapsconect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO simple de materia (id y nombre).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MateriaSimpleDTO {

    private Integer id;
    private String nombre;

}
