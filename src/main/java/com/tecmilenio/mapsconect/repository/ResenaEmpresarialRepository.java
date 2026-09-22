package com.tecmilenio.mapsconect.repository;

import com.tecmilenio.mapsconect.entity.ResenaEmpresarial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResenaEmpresarialRepository extends JpaRepository<ResenaEmpresarial, Integer> {

    boolean existsByIdEstudianteAndIdEmpresa(Integer idEstudiante, Integer idEmpresa);

    Optional<ResenaEmpresarial> findFirstByIdEstudianteAndIdEmpresa(Integer idEstudiante, Integer idEmpresa);

    List<ResenaEmpresarial> findByIdEmpresaOrderByFechaResenaDesc(Integer idEmpresa);

    @Query(value = """
        SELECT r.id_resena, r.calificacion, r.proyecto_desarrollado, r.aprendizajes,
               r.recomendaciones, r.fecha_resena, u.nombre_completo, u.id_usuario,
               e.semestre_actual
        FROM resenas_empresarial r
        JOIN estudiantes e ON e.id_estudiante = r.id_estudiante
        JOIN usuarios u ON u.id_usuario = e.id_usuario
        WHERE r.id_empresa = :idEmpresa
        ORDER BY r.fecha_resena DESC
        """, nativeQuery = true)
    List<Object[]> listarConAutor(@Param("idEmpresa") Integer idEmpresa);
}