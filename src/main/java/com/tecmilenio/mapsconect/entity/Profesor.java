package com.tecmilenio.mapsconect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "profesores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profesor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_profesor")
    private Integer id;

    @Column(name = "id_usuario", nullable = false, unique = true)
    private Integer idUsuario;

    @Column(name = "numero_nomina", nullable = false, unique = true, length = 20)
    private String numeroNomina;

    @Column(name = "area_especialidad", length = 150)
    private String areaEspecialidad;

    @Column(name = "biografia", columnDefinition = "text")
    private String biografia;

    @Column(name = "horario_asesorias", length = 255)
    private String horarioAsesorias;

    @Column(name = "enlace_sala_virtual", length = 255)
    private String enlaceSalaVirtual;

    @Column(name = "semestres_asignados", length = 100)
    private String semestresAsignados;

    @Column(name = "disponible_chat", nullable = false)
    private Boolean disponibleChat;

}