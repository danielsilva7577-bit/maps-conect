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
 * Publicacion en el foro: duda, respuesta o comentario. Puede estar asociada a una materia.
 */
@Entity
@Table(name = "publicaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Publicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_publicacion")
    private Integer id;

    @Column(name = "id_estudiante", nullable = false)
    private Integer idEstudiante;

    @Column(name = "id_carrera", nullable = false)
    private Integer idCarrera;

    @Column(name = "id_materia")
    private Integer idMateria;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "contenido", nullable = false, columnDefinition = "text")
    private String contenido;

    @Column(name = "fecha_publicacion", nullable = false)
    private LocalDateTime fechaPublicacion;

    @Column(name = "estado", nullable = false, length = 20)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String estado;

    @Column(name = "id_respuesta_aceptada")
    private Integer idRespuestaAceptada;

}

