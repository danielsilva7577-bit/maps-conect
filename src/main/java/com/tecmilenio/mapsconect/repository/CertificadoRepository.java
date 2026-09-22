package com.tecmilenio.mapsconect.repository;

import com.tecmilenio.mapsconect.entity.Certificado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de certificados academicos. Busqueda por nombre (ignore case).
 */
@Repository
public interface CertificadoRepository extends JpaRepository<Certificado, Integer> {

    boolean existsByNombreIgnoreCase(String nombre);

}
