package com.tecmilenio.mapsconect.service;

import com.tecmilenio.mapsconect.dto.LoginDTO;
import com.tecmilenio.mapsconect.dto.RegistroDTO;
import com.tecmilenio.mapsconect.dto.TokenDTO;
import com.tecmilenio.mapsconect.entity.Estudiante;
import com.tecmilenio.mapsconect.entity.Profesor;
import com.tecmilenio.mapsconect.entity.Usuario;
import com.tecmilenio.mapsconect.repository.EstudianteRepository;
import com.tecmilenio.mapsconect.repository.EstudianteMateriaRepository;
import com.tecmilenio.mapsconect.repository.ProfesorCertificadoRepository;
import com.tecmilenio.mapsconect.repository.ProfesorMateriaRepository;
import com.tecmilenio.mapsconect.repository.ProfesorRepository;
import com.tecmilenio.mapsconect.repository.UsuarioRepository;
import com.tecmilenio.mapsconect.security.JwtTokenProvider;
import com.tecmilenio.mapsconect.security.LoginRateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para UsuarioService — lógica de negocio del auth y onboarding.
 *
 * Cobertura:
 *  - login exitoso vs credenciales incorrectas
 *  - login con cuenta inactiva
 *  - registro con email duplicado (ConflictoException)
 *  - registro exitoso de estudiante
 *  - registro exitoso de profesor con nómina
 *  - resolverRol con valores desconocidos
 *  - estadoPerfil para estudiante completado y sin completar
 */
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class UsuarioServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private LoginRateLimiter loginRateLimiter;
    @Mock private EstudianteRepository estudianteRepository;
    @Mock private ProfesorRepository profesorRepository;
    @Mock private ProfesorMateriaRepository profesorMateriaRepository;
    @Mock private ProfesorCertificadoRepository profesorCertificadoRepository;
    @Mock private EstudianteMateriaRepository estudianteMateriaRepository;

    @InjectMocks private UsuarioService usuarioService;

    private final String EMAIL = "alumno@tecmilenio.mx";
    private final String PASSWORD_HASH = "$2a$10$hashquemuylargo";
    private final String TOKEN = "jwt-token-de-prueba";

    @BeforeEach
    void setUp() {
        // El rate limiter siempre retorna null (sin bloqueo) en tests unitarios.
        // lenient: no todos los tests llaman a login(), así que este stub es opcional.
        lenient().when(loginRateLimiter.tiempoRestanteBloqueo(anyString())).thenReturn(null);
    }

    // ── LOGIN ──────────────────────────────────────────────────────────────

    @Test
    void login_exitoso_devuelveToken() {
        // Arrange
        Usuario usuario = Usuario.builder()
                .id(1)
                .email(EMAIL)
                .nombreCompleto("Juan Pérez")
                .contrasena(PASSWORD_HASH)
                .rol(Usuario.Rol.ESTUDIANTE)
                .activo(true)
                .build();

        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("DemoMaps2026!", PASSWORD_HASH)).thenReturn(true);
        when(jwtTokenProvider.generateToken(EMAIL)).thenReturn(TOKEN);
        when(jwtTokenProvider.getExpiresInSeconds()).thenReturn(604800L);

        LoginDTO dto = LoginDTO.builder().email(EMAIL).contrasena("DemoMaps2026!").build();

        // Act
        TokenDTO result = usuarioService.login(dto, "127.0.0.1");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo(TOKEN);
        assertThat(result.getUsuario().getId()).isEqualTo(1);
        assertThat(result.getUsuario().getRol()).isEqualTo("ESTUDIANTE");
        verify(loginRateLimiter).registrarExito(anyString());
        verify(loginRateLimiter, never()).registrarFallo(anyString());
    }

    @Test
    void login_conCredencialesIncorrectas_lanzaBadCredentialsException() {
        // Arrange
        Usuario usuario = Usuario.builder()
                .id(1)
                .email(EMAIL)
                .contrasena(PASSWORD_HASH)
                .rol(Usuario.Rol.ESTUDIANTE)
                .activo(true)
                .build();

        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("password-mal", PASSWORD_HASH)).thenReturn(false);

        LoginDTO dto = LoginDTO.builder().email(EMAIL).contrasena("password-mal").build();

        // Act / Assert
        assertThatThrownBy(() -> usuarioService.login(dto, "127.0.0.1"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Credenciales incorrectas");

        verify(loginRateLimiter).registrarFallo(anyString());
    }

    @Test
    void login_conCuentaInactiva_lanzaBadCredentialsException() {
        // Arrange
        Usuario usuario = Usuario.builder()
                .id(1)
                .email(EMAIL)
                .contrasena(PASSWORD_HASH)
                .rol(Usuario.Rol.ESTUDIANTE)
                .activo(false)
                .build();

        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("DemoMaps2026!", PASSWORD_HASH)).thenReturn(true);

        LoginDTO dto = LoginDTO.builder().email(EMAIL).contrasena("DemoMaps2026!").build();

        // Act / Assert
        assertThatThrownBy(() -> usuarioService.login(dto, "127.0.0.1"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Tu cuenta ha sido inhabilitada. Contacta al administrador.");
    }

    @Test
    void login_conEmailInexistente_lanzaBadCredentialsException() {
        // Arrange
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        LoginDTO dto = LoginDTO.builder().email(EMAIL).contrasena("cualquiera").build();

        // Act / Assert
        assertThatThrownBy(() -> usuarioService.login(dto, "127.0.0.1"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Credenciales incorrectas");

        verify(loginRateLimiter).registrarFallo(anyString());
    }

    // ── REGISTRO ─────────────────────────────────────────────────────────────

    @Test
    void registrar_conEmailDuplicado_lanzaConflictoException() {
        // Arrange
        when(usuarioRepository.existsByEmail(EMAIL)).thenReturn(true);

        RegistroDTO dto = RegistroDTO.builder()
                .email(EMAIL)
                .nombre("Juan")
                .apellido("Pérez")
                .contrasena("DemoMaps2026!")
                .rol("ESTUDIANTE")
                .build();

        // Act / Assert
        assertThatThrownBy(() -> usuarioService.registrar(dto))
                .isInstanceOf(com.tecmilenio.mapsconect.exception.ConflictoException.class)
                .hasMessage("El email ya está registrado");
    }

    @Test
    void registrar_exitoso_estudiante_sinNomina() {
        // Arrange
        when(usuarioRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode("DemoMaps2026!")).thenReturn(PASSWORD_HASH);
        when(jwtTokenProvider.generateToken(EMAIL)).thenReturn(TOKEN);
        when(jwtTokenProvider.getExpiresInSeconds()).thenReturn(604800L);

        Usuario usuarioGuardado = Usuario.builder()
                .id(1)
                .email(EMAIL)
                .nombreCompleto("Juan Pérez")
                .contrasena(PASSWORD_HASH)
                .rol(Usuario.Rol.ESTUDIANTE)
                .activo(true)
                .build();

        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado);

        RegistroDTO dto = RegistroDTO.builder()
                .email(EMAIL)
                .nombre("Juan")
                .apellido("Pérez")
                .contrasena("DemoMaps2026!")
                .rol("ESTUDIANTE")
                .build();

        // Act
        TokenDTO result = usuarioService.registrar(dto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo(TOKEN);
        assertThat(result.getUsuario().getRol()).isEqualTo("ESTUDIANTE");
        verify(profesorRepository, never()).save(any());
    }

    @Test
    void registrar_exitoso_profesor_conNomina_creaProfesor() {
        // Arrange
        String profEmail = "prof@tecmilenio.mx";
        when(usuarioRepository.existsByEmail(profEmail)).thenReturn(false);
        when(passwordEncoder.encode("DemoMaps2026!")).thenReturn(PASSWORD_HASH);
        when(jwtTokenProvider.generateToken(profEmail)).thenReturn(TOKEN);
        when(jwtTokenProvider.getExpiresInSeconds()).thenReturn(604800L);
        when(profesorRepository.existsByNumeroNomina("NOM-123")).thenReturn(false);

        Usuario usuarioGuardado = Usuario.builder()
                .id(2)
                .email(profEmail)
                .nombreCompleto("Dra. Ana López")
                .contrasena(PASSWORD_HASH)
                .rol(Usuario.Rol.PROFESOR)
                .activo(true)
                .build();

        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado);

        RegistroDTO dto = RegistroDTO.builder()
                .email(profEmail)
                .nombre("Ana")
                .apellido("López")
                .contrasena("DemoMaps2026!")
                .rol("PROFESOR")
                .numeroNomina("NOM-123")
                .build();

        // Act
        TokenDTO result = usuarioService.registrar(dto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsuario().getRol()).isEqualTo("PROFESOR");
        verify(profesorRepository).save(any(Profesor.class));
    }

    @Test
    void registrar_profesor_sinNomina_lanzaIllegalArgumentException() {
        // Arrange
        when(usuarioRepository.existsByEmail(EMAIL)).thenReturn(false);

        RegistroDTO dto = RegistroDTO.builder()
                .email(EMAIL)
                .nombre("Ana")
                .apellido("López")
                .contrasena("DemoMaps2026!")
                .rol("PROFESOR")
                .numeroNomina(null)
                .build();

        // Act / Assert
        assertThatThrownBy(() -> usuarioService.registrar(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El número de nómina es obligatorio para docentes");
    }

    // ── ESTADO PERFIL ───────────────────────────────────────────────────────

    @Test
    void estadoPerfil_estudianteSinEstudiante_cuentaIncompleta() {
        // Arrange
        Usuario usuario = Usuario.builder()
                .id(1)
                .email(EMAIL)
                .rol(Usuario.Rol.ESTUDIANTE)
                .build();

        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(estudianteRepository.existsByIdUsuario(1)).thenReturn(false);

        // Act
        var estado = usuarioService.estadoPerfil(EMAIL);

        // Assert
        assertThat(estado.get("completado")).isEqualTo(false);
        assertThat(estado.get("rol")).isEqualTo("ESTUDIANTE");
    }

    @Test
    void estadoPerfil_estudianteConEstudiante_cuentaCompleta() {
        // Arrange
        Usuario usuario = Usuario.builder()
                .id(1)
                .email(EMAIL)
                .rol(Usuario.Rol.ESTUDIANTE)
                .build();

        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(estudianteRepository.existsByIdUsuario(1)).thenReturn(true);

        // Act
        var estado = usuarioService.estadoPerfil(EMAIL);

        // Assert
        assertThat(estado.get("completado")).isEqualTo(true);
        assertThat(estado.get("rol")).isEqualTo("ESTUDIANTE");
    }

    @Test
    void estadoPerfil_profesorSinEspecialidad_cuentaIncompleta() {
        // Arrange
        Usuario usuario = Usuario.builder()
                .id(2)
                .email("prof@tecmilenio.mx")
                .rol(Usuario.Rol.PROFESOR)
                .build();

        when(usuarioRepository.findByEmail("prof@tecmilenio.mx")).thenReturn(Optional.of(usuario));
        when(profesorRepository.findByIdUsuario(2)).thenReturn(Optional.of(
                Profesor.builder().areaEspecialidad(null).build()
        ));

        // Act
        var estado = usuarioService.estadoPerfil("prof@tecmilenio.mx");

        // Assert
        assertThat(estado.get("completado")).isEqualTo(false);
        assertThat(estado.get("rol")).isEqualTo("PROFESOR");
    }

    @Test
    void estadoPerfil_admin_siemprequeCompleto() {
        // Arrange
        Usuario usuario = Usuario.builder()
                .id(3)
                .email("admin@tecmilenio.mx")
                .rol(Usuario.Rol.ADMINISTRADOR)
                .build();

        when(usuarioRepository.findByEmail("admin@tecmilenio.mx")).thenReturn(Optional.of(usuario));

        // Act
        var estado = usuarioService.estadoPerfil("admin@tecmilenio.mx");

        // Assert
        assertThat(estado.get("completado")).isEqualTo(true);
        assertThat(estado.get("rol")).isEqualTo("ADMINISTRADOR");
        verifyNoInteractions(estudianteRepository, profesorRepository);
    }

    // ── RESOLUCIÓN DE ROL ──────────────────────────────────────────────────

    @Test
    void estadoPerfil_usuarioNoEncontrado_lanzaResourceNotFoundException() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.estadoPerfil(EMAIL))
                .isInstanceOf(com.tecmilenio.mapsconect.exception.ResourceNotFoundException.class)
                .hasMessage("Usuario no encontrado");
    }

    // ── ONNIBOARDING ───────────────────────────────────────────────────────

    @Test
    void completarOnboardingEstudiante_conSemestreInvalido_lanzaExcepcion() {
        // Arrange
        Usuario usuario = Usuario.builder()
                .id(1)
                .email(EMAIL)
                .rol(Usuario.Rol.ESTUDIANTE)
                .build();
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));

        // Act / Assert — semestre 15 es inválido (max 12)
        assertThatThrownBy(() -> usuarioService.completarOnboardingEstudiante(EMAIL,
                com.tecmilenio.mapsconect.dto.OnboardingEstudianteDTO.builder()
                        .matricula("A01234567")
                        .idCarrera(1)
                        .semestre(15)
                        .build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El semestre debe estar entre 1 y 12");
    }
}


