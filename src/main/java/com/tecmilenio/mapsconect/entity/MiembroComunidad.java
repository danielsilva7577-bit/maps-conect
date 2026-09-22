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
 * Relacion entre un usuario y una comunidad de estudio.
 */
@Entity
@Table(name = "miembros_comunidad")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MiembroComunidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_miembro")
    private Integer id;

    @Column(name = "id_comunidad", nullable = false)
    private Integer idComunidad;

    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    @Column(name = "rol_en_comunidad", nullable = false, length = 10)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String rol;

    @Column(name = "fecha_union", nullable = false)
    private LocalDateTime fechaUnion;

}

