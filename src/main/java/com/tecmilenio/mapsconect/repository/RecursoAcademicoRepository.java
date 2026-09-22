package com.tecmilenio.mapsconect.repository;

import com.tecmilenio.mapsconect.entity.RecursoAcademico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de apuntes y recursos compartidos. Consultas para recursos visibles y estadisticas de descargas.
 */
@Repository
public interface RecursoAcademicoRepository extends JpaRepository<RecursoAcademico, Integer> {

    @Query(value = """
        SELECT r.id_recurso, r.titulo, r.descripcion, r.url_archivo, r.tipo_archivo,
               r.fecha_subida, r.contador_descargas,
               u.nombre_completo AS autor,
               m.nombre_materia AS materia,
               r.adjunto_nombre, r.adjunto_tamano,
               r.id_materia
        FROM recursos_academicos r
        JOIN usuarios u ON u.id_usuario = r.id_usuario
        LEFT JOIN materias m ON m.id_materia = r.id_materia
        WHERE r.oculto = FALSE
        ORDER BY r.fecha_subida DESC
        """, nativeQuery = true)
    List<Object[]> findRecursosVisibles();

    @Query(value = """
        SELECT r.id_recurso, r.titulo, r.descripcion, r.url_archivo, r.tipo_archivo,
               r.fecha_subida, r.contador_descargas,
               u.nombre_completo AS autor,
               m.nombre_materia AS materia,
               r.adjunto_nombre, r.adjunto_tamano
        FROM recursos_academicos r
        JOIN usuarios u ON u.id_usuario = r.id_usuario
        LEFT JOIN materias m ON m.id_materia = r.id_materia
        WHERE r.oculto = FALSE AND r.id_materia = :idMateria
        ORDER BY r.fecha_subida DESC
        """, nativeQuery = true)
    List<Object[]> findRecursosVisiblesPorMateria(@Param("idMateria") int idMateria);

    @Query(value = """
        SELECT COALESCE(SUM(r.contador_descargas), 0)
        FROM recursos_academicos r
        WHERE r.id_usuario = :idUsuario
        """, nativeQuery = true)
    long sumDescargasDeUsuario(@Param("idUsuario") int idUsuario);

    @Query(value = """
        SELECT r.id_recurso, r.titulo, r.descripcion, m.nombre_materia AS materia, u.nombre_completo AS autor, r.tipo_archivo
        FROM recursos_academicos r
        JOIN usuarios u ON u.id_usuario = r.id_usuario
        LEFT JOIN materias m ON m.id_materia = r.id_materia
        WHERE r.oculto = FALSE AND (LOWER(r.titulo) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(r.descripcion) LIKE LOWER(CONCAT('%', :q, '%')))
        ORDER BY r.fecha_subida DESC
        LIMIT 5
        """, nativeQuery = true)
    List<Object[]> buscarRecursosGlobal(@Param("q") String q);

}

