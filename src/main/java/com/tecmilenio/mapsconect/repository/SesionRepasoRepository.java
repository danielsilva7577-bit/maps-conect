package com.tecmilenio.mapsconect.repository;

import com.tecmilenio.mapsconect.entity.SesionRepaso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SesionRepasoRepository extends JpaRepository<SesionRepaso, Integer> {

    @Query(value = """
        SELECT s.id_sesion, s.titulo, s.descripcion, s.materia, s.modalidad, s.ubicacion,
               s.fecha, s.hora_inicio, s.duracion_min, s.cupo_max, s.estado,
               u.nombre_completo AS organizador, s.organizador_id, s.creado_en,
               COUNT(i.id_usuario) AS inscritos,
               MAX(CASE WHEN i.id_usuario = :idUsuario THEN 1 ELSE 0 END) AS inscrito
        FROM sesiones_repaso s
        JOIN usuarios u ON u.id_usuario = s.organizador_id
        LEFT JOIN sesion_inscripciones i ON i.id_sesion = s.id_sesion
        WHERE s.estado <> 'CERRADA'
        GROUP BY s.id_sesion, s.titulo, s.descripcion, s.materia, s.modalidad, s.ubicacion,
                 s.fecha, s.hora_inicio, s.duracion_min, s.cupo_max, s.estado,
                 u.nombre_completo, s.organizador_id, s.creado_en
        ORDER BY s.fecha ASC, s.hora_inicio ASC
        """, nativeQuery = true)
    List<Object[]> listarConInscripciones(@Param("idUsuario") int idUsuario);

    @Query(value = """
        SELECT s.id_sesion, s.titulo, s.descripcion, s.materia, s.modalidad, s.ubicacion,
               s.fecha, s.hora_inicio, s.duracion_min, s.cupo_max, s.estado,
               u.nombre_completo AS organizador, s.organizador_id, s.creado_en,
               COUNT(i.id_usuario) AS inscritos
        FROM sesiones_repaso s
        JOIN usuarios u ON u.id_usuario = s.organizador_id
        LEFT JOIN sesion_inscripciones i ON i.id_sesion = s.id_sesion
        WHERE s.organizador_id = :idUsuario
        GROUP BY s.id_sesion, s.titulo, s.descripcion, s.materia, s.modalidad, s.ubicacion,
                 s.fecha, s.hora_inicio, s.duracion_min, s.cupo_max, s.estado,
                 u.nombre_completo, s.organizador_id, s.creado_en
        ORDER BY s.fecha ASC, s.hora_inicio ASC
        """, nativeQuery = true)
    List<Object[]> findSesionesDeOrganizador(@Param("idUsuario") int idUsuario);
}