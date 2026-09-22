package com.tecmilenio.mapsconect.repository;

import com.tecmilenio.mapsconect.entity.ProfesorCertificado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfesorCertificadoRepository extends JpaRepository<ProfesorCertificado, Integer> {

    void deleteByIdProfesor(Integer idProfesor);

    boolean existsByIdProfesorAndIdCertificado(Integer idProfesor, Integer idCertificado);

}