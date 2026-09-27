package com.tecmilenio.mapsconect.service;

import com.tecmilenio.mapsconect.dto.TipDTO;
import com.tecmilenio.mapsconect.entity.Materia;
import com.tecmilenio.mapsconect.entity.TipAcademico;
import com.tecmilenio.mapsconect.entity.Usuario;
import com.tecmilenio.mapsconect.entity.VotoTip;
import com.tecmilenio.mapsconect.exception.ResourceNotFoundException;
import com.tecmilenio.mapsconect.repository.MateriaRepository;
import com.tecmilenio.mapsconect.repository.TipRepository;
import com.tecmilenio.mapsconect.repository.UsuarioRepository;
import com.tecmilenio.mapsconect.repository.VotoTipRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TipServiceTest {

    @Mock
    private TipRepository tipRepository;

    @Mock
    private VotoTipRepository votoTipRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private MateriaRepository materiaRepository;

    @Mock
    private CarreraContextoService carreraContextoService;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private TipService tipService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tipService, "entityManager", entityManager);
    }

    @Test
    void listarPorMateria_conIdNulo_devuelveListaVacia() {
        List<TipDTO> result = tipService.listarPorMateria(null);
        assertThat(result).isEmpty();
    }

    @Test
    void crear_conUsuarioNoEncontrado_lanzaResourceNotFoundException() {
        when(usuarioRepository.findByEmail("noexiste@tecmilenio.mx")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tipService.crear("noexiste@tecmilenio.mx", "Contenido", 1))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    @Test
    void crear_conMateriaInvalida_lanzaIllegalArgumentException() {
        Usuario usuario = Usuario.builder().id(1).email("test@tecmilenio.mx").build();
        when(usuarioRepository.findByEmail("test@tecmilenio.mx")).thenReturn(Optional.of(usuario));

        jakarta.persistence.Query mockQuery = Mockito.mock(jakarta.persistence.Query.class);
        when(entityManager.createNativeQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyInt(), any())).thenReturn(mockQuery);
        when(mockQuery.getSingleResult()).thenReturn(0L); // no existe

        assertThatThrownBy(() -> tipService.crear("test@tecmilenio.mx", "Contenido", 999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Debes indicar una materia válida");
    }

    @Test
    void votar_cuandoYaSeVoto_lanzaIllegalArgumentException() {
        Usuario usuario = Usuario.builder().id(1).email("test@tecmilenio.mx").build();
        when(usuarioRepository.findByEmail("test@tecmilenio.mx")).thenReturn(Optional.of(usuario));

        TipAcademico tip = TipAcademico.builder().id(10).build();
        when(tipRepository.findById(10)).thenReturn(Optional.of(tip));

        when(votoTipRepository.existsByIdTipAndIdUsuario(10, 1)).thenReturn(true);

        assertThatThrownBy(() -> tipService.votar("test@tecmilenio.mx", 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya has votado este tip");
    }

    @Test
    void desvotar_cuandoNoHayVoto_lanzaIllegalArgumentException() {
        Usuario usuario = Usuario.builder().id(1).email("test@tecmilenio.mx").build();
        when(usuarioRepository.findByEmail("test@tecmilenio.mx")).thenReturn(Optional.of(usuario));

        when(votoTipRepository.findByIdTipAndIdUsuario(10, 1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tipService.desvotar("test@tecmilenio.mx", 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No has votado este tip");
    }

    @Test
    void votar_exitoso_incrementaTotalVotosEnDTODevuelto() {
        Usuario usuario = Usuario.builder().id(1).nombreCompleto("Test User").email("test@tecmilenio.mx").rol(Usuario.Rol.ESTUDIANTE).build();
        when(usuarioRepository.findByEmail("test@tecmilenio.mx")).thenReturn(Optional.of(usuario));

        TipAcademico tip = TipAcademico.builder().id(10).idMateria(5).autor(usuario).contenido("Tip").fechaPublicacion(LocalDateTime.now()).totalVotos(5).build();
        when(tipRepository.findById(10)).thenReturn(Optional.of(tip));

        when(votoTipRepository.existsByIdTipAndIdUsuario(10, 1)).thenReturn(false);

        // mock para obtenerNombreMateria en mapearADTO
        jakarta.persistence.Query mockQuery = Mockito.mock(jakarta.persistence.Query.class);
        when(entityManager.createNativeQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyInt(), any())).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(List.of());

        TipDTO dto = tipService.votar("test@tecmilenio.mx", 10);

        assertThat(dto.getVotos()).isEqualTo(6); // Se incrementó en memoria
    }
}
