package com.tecmilenio.mapsconect.service;

import com.tecmilenio.mapsconect.dto.EstudianteDTO;
import com.tecmilenio.mapsconect.dto.EstudianteRequestDTO;
import com.tecmilenio.mapsconect.entity.Estudiante;
import com.tecmilenio.mapsconect.entity.Usuario;
import com.tecmilenio.mapsconect.exception.ResourceNotFoundException;
import com.tecmilenio.mapsconect.repository.EstudianteRepository;
import com.tecmilenio.mapsconect.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EstudianteService {

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<EstudianteDTO> listar() {
        return estudianteRepository.findAll().stream().map(this::mapearADTO).toList();
    }

    public EstudianteDTO obtenerPorId(Integer id) {
        Estudiante estudiante = estudianteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado"));
        return mapearADTO(estudiante);
    }

    public EstudianteDTO obtenerPerfilActual(String email) {
        Usuario usuario = obtenerUsuario(email);
        Estudiante estudiante = estudianteRepository.findByUsuario_Id(usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Aún no has completado tu perfil de estudiante"));
        return mapearADTO(estudiante);
    }

    public EstudianteDTO crearPerfil(String email, EstudianteRequestDTO dto) {
        Usuario usuario = obtenerUsuario(email);

        if (estudianteRepository.findByUsuario_Id(usuario.getId()).isPresent()) {
            throw new RuntimeException("Ya existe un perfil de estudiante para este usuario");
        }
        if (estudianteRepository.existsByMatricula(dto.getMatricula())) {
            throw new RuntimeException("La matrícula ya está registrada");
        }

        Estudiante estudiante = Estudiante.builder()
                .usuario(usuario)
                .idCarrera(dto.getIdCarrera())
                .matricula(dto.getMatricula())
                .semestreActual(dto.getSemestreActual())
                .propositoVida(dto.getPropositoVida())
                .puntosReputacion(0)
                .build();

        return mapearADTO(estudianteRepository.save(estudiante));
    }

    public EstudianteDTO actualizarPerfil(String email, EstudianteRequestDTO dto) {
        Usuario usuario = obtenerUsuario(email);
        Estudiante estudiante = estudianteRepository.findByUsuario_Id(usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Aún no has completado tu perfil de estudiante"));

        estudiante.setIdCarrera(dto.getIdCarrera());
        estudiante.setSemestreActual(dto.getSemestreActual());
        estudiante.setPropositoVida(dto.getPropositoVida());

        return mapearADTO(estudianteRepository.save(estudiante));
    }

    private Usuario obtenerUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private EstudianteDTO mapearADTO(Estudiante estudiante) {
        Usuario usuario = estudiante.getUsuario();
        return EstudianteDTO.builder()
                .id(estudiante.getId())
                .idUsuario(usuario != null ? usuario.getId() : null)
                .nombreCompleto(usuario != null ? (usuario.getNombre() + " " + usuario.getApellido()) : null)
                .email(usuario != null ? usuario.getEmail() : null)
                .idCarrera(estudiante.getIdCarrera())
                .matricula(estudiante.getMatricula())
                .semestreActual(estudiante.getSemestreActual())
                .propositoVida(estudiante.getPropositoVida())
                .puntosReputacion(estudiante.getPuntosReputacion())
                .build();
    }

}
