package com.tecmilenio.mapsconect.repository;

import com.tecmilenio.mapsconect.entity.ResenaEmpresarial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResenaEmpresarialRepository extends JpaRepository<ResenaEmpresarial, Integer> {

    List<ResenaEmpresarial> findByEmpresa_Id(Integer idEmpresa);

}
