package com.tecmilenio.mapsconect.repository;

import com.tecmilenio.mapsconect.entity.Materia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MateriaRepository extends JpaRepository<Materia, Integer> {

    List<Materia> findByTipo(Materia.Tipo tipo);

    Optional<Materia> findByClave(String clave);

}
