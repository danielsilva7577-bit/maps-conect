package com.tecmilenio.mapsconect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad Respuesta que representa una solución o comentario aportado a una publicación del foro.
 */
@Entity
@Table(name = "respuestas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Respuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_respuesta")
    private Integer id;

    @Column(name = "id_publicacion", nullable = false)
    private Integer idPublicacion;

    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    @Column(name = "contenido", nullable = false, columnDefinition = "text")
    private String contenido;

    @Column(name = "fecha_respuesta", nullable = false)
    private LocalDateTime fechaRespuesta;

    @Column(name = "es_verificada_docente", nullable = false)
    private boolean esVerificadaDocente;

    @Column(name = "es_solucion", nullable = false)
    private boolean esSolucion;

}
