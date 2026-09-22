package com.tecmilenio.mapsconect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "sesion_inscripciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SesionInscripcion {

    @EmbeddedId
    private SesionInscripcionId id;

    @Column(name = "fecha_inscripcion", nullable = false)
    private LocalDateTime fechaInscripcion;

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SesionInscripcionId implements java.io.Serializable {
        @Column(name = "id_sesion")
        private Integer idSesion;

        @Column(name = "id_usuario")
        private Integer idUsuario;
    }

}