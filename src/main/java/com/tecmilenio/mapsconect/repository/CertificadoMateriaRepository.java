package com.tecmilenio.mapsconect.repository;

import com.tecmilenio.mapsconect.entity.CertificadoMateria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de la relacion muchos-a-muchos entre certificados y materias.
 */
@Repository
public interface CertificadoMateriaRepository extends JpaRepository<CertificadoMateria, Integer> {

    List<CertificadoMateria> findByIdCertificadoOrderByIdAsc(Integer idCertificado);

    boolean existsByIdCertificadoAndIdMateria(Integer idCertificado, Integer idMateria);

}
