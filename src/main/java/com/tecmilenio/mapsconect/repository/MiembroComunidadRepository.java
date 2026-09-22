package com.tecmilenio.mapsconect.repository;

import com.tecmilenio.mapsconect.entity.MiembroComunidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de miembros de comunidades de estudio. Consultas para grupos de un usuario y estadisticas.
 */
@Repository
public interface MiembroComunidadRepository extends JpaRepository<MiembroComunidad, Integer> {

    @Query(value = """
        SELECT c.id_comunidad, c.nombre_comunidad,
               (SELECT COUNT(*) FROM miembros_comunidad mc WHERE mc.id_comunidad = c.id_comunidad) AS miembros,
               m.nombre_materia AS materia
        FROM miembros_comunidad mc
        JOIN comunidades_estudio c ON c.id_comunidad = mc.id_comunidad
        LEFT JOIN materias m ON m.id_materia = c.id_materia
        WHERE mc.id_usuario = :idUsuario
        ORDER BY c.fecha_creacion DESC
        """, nativeQuery = true)
    List<Object[]> findGruposDeUsuario(@Param("idUsuario") Integer idUsuario);

}

