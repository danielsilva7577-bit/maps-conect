package com.tecmilenio.mapsconect.repository;

import com.tecmilenio.mapsconect.entity.EstudianteCertificado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de asignaciones de certificados a estudiantes. Ordena por fecha de seleccion.
 */
@Repository
public interface EstudianteCertificadoRepository extends JpaRepository<EstudianteCertificado, Integer> {

    List<EstudianteCertificado> findByIdEstudianteOrderByFechaSeleccionAsc(Integer idEstudiante);

    boolean existsByIdEstudianteAndIdCertificado(Integer idEstudiante, Integer idCertificado);

    long countByIdEstudiante(Integer idEstudiante);

}
