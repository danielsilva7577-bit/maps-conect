package com.tecmilenio.mapsconect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Comunidad de estudio (grupo de estudiantes con intereses comunes).
 */
@Entity
@Table(name = "comunidades_estudio")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComunidadEstudio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comunidad")
    private Integer id;

    @Column(name = "nombre_comunidad", nullable = false, length = 150)
    private String nombre;

    @Column(name = "id_creador", nullable = false)
    private Integer idCreador;

    @Column(name = "id_carrera")
    private Integer idCarrera;

    @Column(name = "id_materia")
    private Integer idMateria;

    @Column(name = "id_certificado")
    private Integer idCertificado;

    @Column(name = "privacidad", nullable = false, length = 10)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String privacidad;

    @Column(name = "enlace_sala_virtual", length = 500)
    private String enlaceSalaVirtual;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

}

