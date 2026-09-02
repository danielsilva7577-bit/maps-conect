package com.tecmilenio.mapsconect.repository;

import com.tecmilenio.mapsconect.entity.EmpresaVinculada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmpresaVinculadaRepository extends JpaRepository<EmpresaVinculada, Integer> {

    List<EmpresaVinculada> findByActivaTrueAndNombreContainingIgnoreCase(String nombre);

    List<EmpresaVinculada> findByActivaTrue();

}
