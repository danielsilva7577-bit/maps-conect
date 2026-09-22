package com.tecmilenio.mapsconect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DTO que agrupa los resultados de busqueda global por categorias.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusquedaGlobalDTO {

    @Builder.Default
    private List<Map<String, Object>> personas = new ArrayList<>();

    @Builder.Default
    private List<Map<String, Object>> materias = new ArrayList<>();

    @Builder.Default
    private List<Map<String, Object>> recursos = new ArrayList<>();

    @Builder.Default
    private List<Map<String, Object>> foro = new ArrayList<>();

}
