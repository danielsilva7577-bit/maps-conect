package com.tecmilenio.mapsconect.repository;

import com.tecmilenio.mapsconect.entity.Respuesta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RespuestaRepository extends JpaRepository<Respuesta, Integer> {

    List<Respuesta> findByIdPublicacionOrderByFechaRespuestaAsc(Integer idPublicacion);

    long countByIdPublicacion(Integer idPublicacion);

    long countByIdUsuario(Integer idUsuario);

}
