package com.tecmilenio.mapsconect.repository;

import com.tecmilenio.mapsconect.entity.EmpresaVinculada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de empresas vinculadas. Consultas para listar empresas con promedio de calificaciones.
 */
@Repository
public interface EmpresaVinculadaRepository extends JpaRepository<EmpresaVinculada, Integer> {

    @Query(value = """
        SELECT e.id_empresa, e.nombre_empresa, e.sector, e.sitio_web, e.descripcion, e.carreras_afines,
               (SELECT AVG(r.calificacion) FROM resenas_empresarial r WHERE r.id_empresa = e.id_empresa) AS calificacion,
               (SELECT COUNT(*) FROM resenas_empresarial r WHERE r.id_empresa = e.id_empresa) AS total_resenas
        FROM empresas_vinculadas e
        ORDER BY e.nombre_empresa ASC
        """, nativeQuery = true)
    List<Object[]> findEmpresasConPromedio();

    @Query(value = """
        SELECT e.id_empresa, e.nombre_empresa, e.sector, e.sitio_web, e.descripcion, e.carreras_afines,
               (SELECT AVG(r.calificacion) FROM resenas_empresarial r WHERE r.id_empresa = e.id_empresa) AS calificacion,
               (SELECT COUNT(*) FROM resenas_empresarial r WHERE r.id_empresa = e.id_empresa) AS total_resenas
        FROM empresas_vinculadas e
        WHERE e.nombre_empresa LIKE %:busqueda%
           OR e.sector LIKE %:busqueda%
           OR e.descripcion LIKE %:busqueda%
           OR e.carreras_afines LIKE %:busqueda%
        ORDER BY e.nombre_empresa ASC
        """, nativeQuery = true)
    List<Object[]> findEmpresasConFiltro(String busqueda);

}

