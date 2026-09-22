package com.tecmilenio.mapsconect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Apunte o material academico compartido por un usuario (PDF, imagen, enlace, etc.).
 */
@Entity
@Table(name = "recursos_academicos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecursoAcademico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recurso")
    private Integer id;

    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    @Column(name = "id_materia", nullable = false)
    private Integer idMateria;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "descripcion", columnDefinition = "text")
    private String descripcion;

    @Column(name = "url_archivo", nullable = false, length = 500)
    private String urlArchivo;

    @Column(name = "adjunto_nombre")
    private String adjuntoNombre;

    @Column(name = "adjunto_tamano")
    private Long adjuntoTamano;

    @Column(name = "tipo_archivo", length = 50)
    private String tipoArchivo;

    @Column(name = "fecha_subida", nullable = false)
    private LocalDateTime fechaSubida;

    @Column(name = "contador_descargas", nullable = false)
    private Integer contadorDescargas;

    @Column(name = "contador_reportes", nullable = false)
    private Integer contadorReportes;

    @Column(name = "oculto", nullable = false)
    private Boolean oculto;

}

