package com.tecmilenio.mapsconect.service;

import com.tecmilenio.mapsconect.dto.CrearPublicacionDTO;
import com.tecmilenio.mapsconect.dto.CrearRespuestaDTO;
import com.tecmilenio.mapsconect.dto.DuplicadoDTO;
import com.tecmilenio.mapsconect.entity.Usuario;
import com.tecmilenio.mapsconect.exception.ResourceNotFoundException;
import com.tecmilenio.mapsconect.repository.EstudianteRepository;
import com.tecmilenio.mapsconect.repository.MateriaRepository;
import com.tecmilenio.mapsconect.repository.PublicacionRepository;
import com.tecmilenio.mapsconect.repository.RecursoAcademicoRepository;
import com.tecmilenio.mapsconect.repository.RespuestaRepository;
import com.tecmilenio.mapsconect.repository.SeguimientoRepository;
import com.tecmilenio.mapsconect.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ForoServiceTest {

    @Mock private PublicacionRepository publicacionRepository;
    @Mock private EstudianteRepository estudianteRepository;
    @Mock private MateriaRepository materiaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private SeguimientoRepository seguimientoRepository;
    @Mock private RecursoAcademicoRepository recursoAcademicoRepository;
    @Mock private RespuestaRepository respuestaRepository;
    @Mock private TipService tipService;
    @Mock private CarreraContextoService carreraContextoService;
    @Mock private NotificacionService notificacionService;
    @Mock private EntityManager entityManager;

    @InjectMocks
    private ForoService foroService;

    @Test
    void publicar_conUsuarioNoEncontrado_lanzaResourceNotFoundException() {
        when(usuarioRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        CrearPublicacionDTO req = new CrearPublicacionDTO();
        assertThatThrownBy(() -> foroService.publicar("noexiste@test.com", req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    @Test
    void publicar_conUsuarioQueNoEsEstudiante_lanzaIllegalArgumentException() {
        Usuario profesor = Usuario.builder().id(1).email("prof@test.com").rol(Usuario.Rol.PROFESOR).build();
        when(usuarioRepository.findByEmail("prof@test.com")).thenReturn(Optional.of(profesor));

        CrearPublicacionDTO req = new CrearPublicacionDTO();
        assertThatThrownBy(() -> foroService.publicar("prof@test.com", req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Solo los estudiantes pueden publicar dudas");
    }

    @Test
    void publicar_conEstudianteSinOnboardingCompleto_lanzaIllegalArgumentException() {
        Usuario estudiante = Usuario.builder().id(1).email("est@test.com").rol(Usuario.Rol.ESTUDIANTE).build();
        when(usuarioRepository.findByEmail("est@test.com")).thenReturn(Optional.of(estudiante));
        when(estudianteRepository.findByIdUsuario(1)).thenReturn(Optional.empty());

        CrearPublicacionDTO req = new CrearPublicacionDTO();
        assertThatThrownBy(() -> foroService.publicar("est@test.com", req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tu perfil de estudiante no está completo");
    }

    @Test
    void responder_aPublicacionNoEncontrada_lanzaResourceNotFoundException() {
        when(publicacionRepository.findById(999)).thenReturn(Optional.empty());

        CrearRespuestaDTO req = new CrearRespuestaDTO();
        assertThatThrownBy(() -> foroService.responder(999, "email@test.com", req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Publicación no encontrada");
    }

    @Test
    void listarRespuestas_conPublicacionNoEncontrada_lanzaResourceNotFoundException() {
        when(publicacionRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> foroService.listarRespuestas(999))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Publicación no encontrada");
    }

    @Test
    void buscarDuplicado_cuandoIgnorarDuplicadoEsTrue_devuelveEmpty() {
        CrearPublicacionDTO req = new CrearPublicacionDTO();
        req.setIgnorarDuplicado(true);

        Optional<DuplicadoDTO> res = foroService.buscarDuplicado("est@test.com", req);
        assertThat(res).isEmpty();
    }

    @Test
    void buscarDuplicado_conUsuarioNoEncontrado_lanzaResourceNotFoundException() {
        when(usuarioRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        CrearPublicacionDTO req = new CrearPublicacionDTO();
        req.setIgnorarDuplicado(false);

        assertThatThrownBy(() -> foroService.buscarDuplicado("noexiste@test.com", req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");
    }
}
