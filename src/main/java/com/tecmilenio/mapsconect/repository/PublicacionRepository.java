package com.tecmilenio.mapsconect.repository;

import com.tecmilenio.mapsconect.entity.Publicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de publicaciones del foro. Consultas nativas complejas para el feed, detalles de respuestas, votos y estadisticas por materia.
 */
@Repository
public interface PublicacionRepository extends JpaRepository<Publicacion, Integer> {

    @Query(value = """
        SELECT p.id_publicacion, p.titulo, p.contenido, p.estado,
               p.fecha_publicacion, p.id_materia,
               u.nombre_completo AS autor,
               m.nombre_materia AS materia,
               (SELECT COUNT(*) FROM respuestas r WHERE r.id_publicacion = p.id_publicacion) AS respuestas,
               CASE WHEN p.estado = 'resuelta' THEN TRUE ELSE FALSE END AS resuelto,
               (SELECT r2.contenido FROM respuestas r2 WHERE r2.id_respuesta = p.id_respuesta_aceptada) AS solucion,
               (SELECT e.semestre_actual FROM estudiantes e WHERE e.id_estudiante = p.id_estudiante) AS semestre,
               u.id_usuario AS autor_id,
               u.foto_url AS autor_foto
        FROM publicaciones p
        JOIN usuarios u ON u.id_usuario = (SELECT e.id_usuario FROM estudiantes e WHERE e.id_estudiante = p.id_estudiante)
        LEFT JOIN materias m ON m.id_materia = p.id_materia
        ORDER BY p.fecha_publicacion DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findForoConDetalles(@Param("limit") int limit);

    @Query(value = """
        SELECT r.id_publicacion, r.contenido, r.es_solucion
        FROM respuestas r
        WHERE r.id_usuario = :idUsuario
        ORDER BY r.id_respuesta
        """, nativeQuery = true)
    List<Object[]> findRespuestasDeUsuario(@Param("idUsuario") int idUsuario);

    long countByIdEstudiante(Integer idEstudiante);

    @Query(value = """
        SELECT p.id_publicacion, p.titulo, p.contenido, p.id_materia,
               u.nombre_completo AS autor,
               m.nombre_materia AS materia
        FROM publicaciones p
        JOIN usuarios u ON u.id_usuario = (SELECT e.id_usuario FROM estudiantes e WHERE e.id_estudiante = p.id_estudiante)
        LEFT JOIN materias m ON m.id_materia = p.id_materia
        WHERE p.estado = 'abierta'
        ORDER BY p.fecha_publicacion DESC
        """, nativeQuery = true)
    List<Object[]> findDudasAbiertasConDetalles();

    long countByEstadoIgnoreCase(String estado);

    @Query(value = "SELECT COUNT(*) FROM respuestas WHERE id_usuario = :idUsuario", nativeQuery = true)
    long countRespuestasDeUsuario(@Param("idUsuario") int idUsuario);

    @Query(value = """
        SELECT p.titulo, p.contenido, p.fecha_publicacion,
               (SELECT COUNT(*) FROM respuestas r WHERE r.id_publicacion = p.id_publicacion) AS respuestas
        FROM publicaciones p
        WHERE p.id_estudiante = :idEstudiante
        ORDER BY p.fecha_publicacion DESC
        LIMIT :limite
        """, nativeQuery = true)
    List<Object[]> findAportesRecientesDeEstudiante(@Param("idEstudiante") int idEstudiante, @Param("limite") int limite);

    @Query(value = """
        SELECT r.id_publicacion, r.contenido, r.es_solucion, p.titulo
        FROM respuestas r
        LEFT JOIN publicaciones p ON p.id_publicacion = r.id_publicacion
        WHERE r.id_usuario = :idUsuario
        ORDER BY r.id_respuesta DESC
        LIMIT :limite
        """, nativeQuery = true)
    List<Object[]> findUltimasRespuestasDeUsuario(@Param("idUsuario") int idUsuario, @Param("limite") int limite);

@Query(value = """
        SELECT r.id_respuesta, r.id_publicacion, r.contenido, r.es_solucion, r.fecha_respuesta,
               u.id_usuario, u.nombre_completo, u.foto_url, r.es_verificada_docente
        FROM respuestas r
        JOIN usuarios u ON u.id_usuario = r.id_usuario
        WHERE r.id_publicacion = :idPublicacion
        ORDER BY r.fecha_respuesta ASC
        """, nativeQuery = true)
    List<Object[]> findRespuestasConAutor(@Param("idPublicacion") int idPublicacion);

    @Query(value = """
        SELECT p.id_publicacion, p.titulo, p.contenido,
               p.fecha_publicacion, p.id_materia,
               u.nombre_completo AS autor,
               m.nombre_materia AS materia,
               CASE WHEN p.estado = 'resuelta' THEN TRUE ELSE FALSE END AS resuelto,
               (SELECT r2.contenido FROM respuestas r2 WHERE r2.id_respuesta = p.id_respuesta_aceptada) AS solucion,
               u.id_usuario AS autor_id,
               (SELECT e.semestre_actual FROM estudiantes e WHERE e.id_estudiante = p.id_estudiante) AS semestre,
               u.foto_url AS autor_foto
        FROM publicaciones p
        JOIN usuarios u ON u.id_usuario = (SELECT e.id_usuario FROM estudiantes e WHERE e.id_estudiante = p.id_estudiante)
        LEFT JOIN materias m ON m.id_materia = p.id_materia
        WHERE p.id_publicacion = :idPublicacion
        """, nativeQuery = true)
    List<Object[]> findForoDetallePorId(@Param("idPublicacion") int idPublicacion);

    @Query(value = """
        SELECT p.id_publicacion, p.titulo, p.contenido, p.estado,
               p.fecha_publicacion, p.id_materia,
               u.nombre_completo AS autor,
               m.nombre_materia AS materia,
               (SELECT COUNT(*) FROM respuestas r WHERE r.id_publicacion = p.id_publicacion) AS respuestas,
               CASE WHEN p.estado = 'resuelta' THEN TRUE ELSE FALSE END AS resuelto,
               (SELECT r2.contenido FROM respuestas r2 WHERE r2.id_respuesta = p.id_respuesta_aceptada) AS solucion,
               (SELECT e.semestre_actual FROM estudiantes e WHERE e.id_estudiante = p.id_estudiante) AS semestre,
               u.id_usuario AS autor_id,
               u.foto_url AS autor_foto
        FROM publicaciones p
        JOIN usuarios u ON u.id_usuario = (SELECT e.id_usuario FROM estudiantes e WHERE e.id_estudiante = p.id_estudiante)
        LEFT JOIN materias m ON m.id_materia = p.id_materia
        ORDER BY p.fecha_publicacion DESC
        LIMIT :limit OFFSET :offset
        """, nativeQuery = true)
    List<Object[]> findForoConDetallesPaginado(@Param("limit") int limit, @Param("offset") int offset);

    @Query(value = """
        SELECT p.id_publicacion, p.titulo, p.contenido, m.nombre_materia AS materia, u.nombre_completo AS autor
        FROM publicaciones p
        JOIN usuarios u ON u.id_usuario = (SELECT e.id_usuario FROM estudiantes e WHERE e.id_estudiante = p.id_estudiante)
        LEFT JOIN materias m ON m.id_materia = p.id_materia
        WHERE LOWER(p.titulo) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(p.contenido) LIKE LOWER(CONCAT('%', :q, '%'))
        ORDER BY p.fecha_publicacion DESC
        LIMIT 5
        """, nativeQuery = true)
    List<Object[]> buscarPublicacionesGlobal(@Param("q") String q);

}

