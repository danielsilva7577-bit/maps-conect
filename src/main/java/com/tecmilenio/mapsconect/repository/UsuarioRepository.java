package com.tecmilenio.mapsconect.repository;

import com.tecmilenio.mapsconect.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de usuarios. Consultas personalizadas para buscar por email, rol, puntos de reputacion y estadisticas de publicaciones.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Usuario> findAllByActivoTrueOrderByPuntosReputacionDesc();

    Optional<Usuario> findFirstByNombreCompleto(String nombreCompleto);

    List<Usuario> findTop8ByActivoTrueAndNombreCompletoContainingIgnoreCaseOrderByPuntosReputacionDesc(String nombre);

    List<Usuario> findTop5ByActivoTrueAndNombreCompletoContainingIgnoreCaseOrderByPuntosReputacionDesc(String nombre);

    List<Usuario> findByRolAndActivoTrue(Usuario.Rol rol);

}

