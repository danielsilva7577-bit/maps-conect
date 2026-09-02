package com.tecmilenio.mapsconect.service;

import com.tecmilenio.mapsconect.dto.EmpresaDTO;
import com.tecmilenio.mapsconect.dto.ExperienciaDTO;
import com.tecmilenio.mapsconect.dto.ResenaDTO;
import com.tecmilenio.mapsconect.dto.ResenaRequestDTO;
import com.tecmilenio.mapsconect.entity.EmpresaVinculada;
import com.tecmilenio.mapsconect.entity.Estudiante;
import com.tecmilenio.mapsconect.entity.ResenaEmpresarial;
import com.tecmilenio.mapsconect.entity.Usuario;
import com.tecmilenio.mapsconect.exception.ResourceNotFoundException;
import com.tecmilenio.mapsconect.repository.EmpresaVinculadaRepository;
import com.tecmilenio.mapsconect.repository.EstudianteRepository;
import com.tecmilenio.mapsconect.repository.ResenaEmpresarialRepository;
import com.tecmilenio.mapsconect.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class EmpresarialService {

    @Autowired
    private EmpresaVinculadaRepository empresaRepository;

    @Autowired
    private ResenaEmpresarialRepository resenaRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<EmpresaDTO> listarEmpresas(String busqueda, Double calificacionMinima) {
        List<EmpresaVinculada> empresas = (busqueda != null && !busqueda.isBlank())
                ? empresaRepository.findByActivaTrueAndNombreContainingIgnoreCase(busqueda)
                : empresaRepository.findByActivaTrue();

        return empresas.stream()
                .map(this::mapearAEmpresaDTO)
                .filter(dto -> calificacionMinima == null
                        || dto.getCalificacion() == null
                        || dto.getCalificacion() >= calificacionMinima)
                .toList();
    }

    public List<ResenaDTO> listarResenasPorEmpresa(Integer idEmpresa) {
        if (!empresaRepository.existsById(idEmpresa)) {
            throw new ResourceNotFoundException("Empresa no encontrada");
        }
        return resenaRepository.findByEmpresa_Id(idEmpresa).stream().map(this::mapearAResenaDTO).toList();
    }

    public ResenaDTO crearResena(String email, ResenaRequestDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Estudiante estudiante = estudianteRepository.findByUsuario_Id(usuario.getId())
                .orElseThrow(() -> new RuntimeException("Debes completar tu perfil de estudiante antes de dejar una reseña"));

        EmpresaVinculada empresa = empresaRepository.findById(dto.getIdEmpresa())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));

        ResenaEmpresarial resena = ResenaEmpresarial.builder()
                .estudiante(estudiante)
                .empresa(empresa)
                .calificacion(dto.getCalificacion())
                .proyectoRealizado(dto.getProyectoRealizado())
                .recomendaciones(dto.getRecomendaciones())
                .build();

        return mapearAResenaDTO(resenaRepository.save(resena));
    }

    private EmpresaDTO mapearAEmpresaDTO(EmpresaVinculada empresa) {
        List<ResenaEmpresarial> resenas = resenaRepository.findByEmpresa_Id(empresa.getId());

        Double promedio = resenas.isEmpty() ? null
                : resenas.stream().mapToInt(ResenaEmpresarial::getCalificacion).average().orElse(0);

        ResenaEmpresarial mejorResena = resenas.stream()
                .max(Comparator.comparingInt(ResenaEmpresarial::getCalificacion))
                .orElse(null);

        ExperienciaDTO experiencia = null;
        if (mejorResena != null) {
            Usuario autorUsuario = mejorResena.getEstudiante().getUsuario();
            experiencia = ExperienciaDTO.builder()
                    .autor(autorUsuario != null ? autorUsuario.getNombre() : "Estudiante")
                    .semestre(mejorResena.getEstudiante().getSemestreActual())
                    .texto(mejorResena.getRecomendaciones() != null
                            ? mejorResena.getRecomendaciones()
                            : mejorResena.getProyectoRealizado())
                    .build();
        }

        return EmpresaDTO.builder()
                .id(empresa.getId())
                .nombre(empresa.getNombre())
                .sector(empresa.getSector())
                .descripcion(empresa.getDescripcion())
                .activa(empresa.getActiva())
                .calificacion(promedio)
                .totalResenas((long) resenas.size())
                .experienciaDestacada(experiencia)
                .build();
    }

    private ResenaDTO mapearAResenaDTO(ResenaEmpresarial resena) {
        Usuario usuario = resena.getEstudiante().getUsuario();
        return ResenaDTO.builder()
                .id(resena.getId())
                .idEmpresa(resena.getEmpresa().getId())
                .idEstudiante(resena.getEstudiante().getId())
                .nombreEstudiante(usuario != null ? (usuario.getNombre() + " " + usuario.getApellido()) : null)
                .calificacion(resena.getCalificacion())
                .proyectoRealizado(resena.getProyectoRealizado())
                .recomendaciones(resena.getRecomendaciones())
                .fechaResena(resena.getFechaResena())
                .build();
    }

}
