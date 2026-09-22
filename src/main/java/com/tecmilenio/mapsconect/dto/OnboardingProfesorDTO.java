package com.tecmilenio.mapsconect.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnboardingProfesorDTO {

    @NotBlank(message = "La nómina es obligatoria")
    private String numeroNomina;

    private String areaEspecialidad;

    private String semestresAsignados;

    private String horarioAsesorias;

    private String enlaceSalaVirtual;

    private String semblanza;

    @Builder.Default
    private List<Integer> certificados = new ArrayList<>();

    @Builder.Default
    private List<Integer> materias = new ArrayList<>();

}