package com.tecmilenio.mapsconect.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tecmilenio.mapsconect.dto.LoginDTO;
import com.tecmilenio.mapsconect.dto.RegistroDTO;
import com.tecmilenio.mapsconect.dto.TokenDTO;
import com.tecmilenio.mapsconect.dto.UsuarioDTO;
import com.tecmilenio.mapsconect.security.JwtTokenProvider;
import com.tecmilenio.mapsconect.security.LoginRateLimiter;
import com.tecmilenio.mapsconect.security.RateLimitingProperties;
import com.tecmilenio.mapsconect.security.RateLimiterService;
import com.tecmilenio.mapsconect.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests para AuthController — endpoints /auth/registrar, /auth/login y /auth/perfil-estado.
 *
 * Usa @WebMvcTest para aislar la capa de presentación: valida @Valid, JSON
 * serialization y el contrato de respuesta ApiResponse, sin levantar el
 * contexto de seguridad completo ni la base de datos.
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private UsuarioService usuarioService;
    // Estos beans son requeridos en el classpath por el contexto de seguridad,
    // aunque AuthController no los use directamente.
    @MockBean private JwtTokenProvider jwtTokenProvider;
    @MockBean private LoginRateLimiter loginRateLimiter;
    // RateLimiterService es necesario porque CorsConfig registra el interceptor.
    @MockBean private RateLimiterService rateLimiterService;
    // RateLimitingProperties es necesario para el interceptor de rate limiting.
    @MockBean private RateLimitingProperties rateLimitingProperties;

    private final String EMAIL = "alumno@tecmilenio.mx";
    private final String CONTRASENA = "DemoMaps2026!";
    private final String TOKEN = "jwt-token-prueba";

    // ── LOGIN ──────────────────────────────────────────────────────────────

    @Test
    void login_exitoso_devuelve200ConToken() throws Exception {
        // Arrange
        UsuarioDTO dto = UsuarioDTO.builder()
                .id(1).email(EMAIL).nombre("Juan Pérez")
                .rol("ESTUDIANTE").activo(true)
                .build();

        TokenDTO tokenDTO = TokenDTO.builder()
                .token(TOKEN).tipo("Bearer").expiresIn(604800L)
                .usuario(dto)
                .build();

        when(usuarioService.login(any(LoginDTO.class), anyString())).thenReturn(tokenDTO);

        LoginDTO body = LoginDTO.builder().email(EMAIL).contrasena(CONTRASENA).build();

        // Act / Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Sesión iniciada correctamente"))
                .andExpect(jsonPath("$.data.token").value(TOKEN))
                .andExpect(jsonPath("$.data.usuario.email").value(EMAIL));

        verify(usuarioService).login(any(LoginDTO.class), anyString());
    }

    @Test
    void login_conEmailInvalido_devuelve400() throws Exception {
        // Arrange — email sin formato válido
        LoginDTO body = LoginDTO.builder().email("no-es-un-email").contrasena(CONTRASENA).build();

        // Act / Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void login_conContrasenaFaltante_devuelve400() throws Exception {
        // Arrange
        LoginDTO body = LoginDTO.builder().email(EMAIL).contrasena("").build();

        // Act / Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void login_conCredencialesIncorrectas_devuelve401() throws Exception {
        // Arrange
        when(usuarioService.login(any(LoginDTO.class), anyString()))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException(
                        "Credenciales incorrectas"));

        LoginDTO body = LoginDTO.builder().email(EMAIL).contrasena("wrong").build();

        // Act / Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Credenciales incorrectas"));
    }

    // ── REGISTRO ───────────────────────────────────────────────────────────

    @Test
    void registrar_exitoso_estudiante_devuelve201ConToken() throws Exception {
        // Arrange
        UsuarioDTO dto = UsuarioDTO.builder()
                .id(2).email(EMAIL).nombre("Juan Pérez")
                .rol("ESTUDIANTE").activo(true)
                .build();

        TokenDTO tokenDTO = TokenDTO.builder()
                .token(TOKEN).tipo("Bearer").expiresIn(604800L)
                .usuario(dto)
                .build();

        when(usuarioService.registrar(any(RegistroDTO.class))).thenReturn(tokenDTO);

        RegistroDTO body = RegistroDTO.builder()
                .email(EMAIL)
                .nombre("Juan")
                .apellido("Pérez")
                .contrasena(CONTRASENA)
                .rol("ESTUDIANTE")
                .build();

        // Act / Assert
        mockMvc.perform(post("/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Usuario registrado correctamente"))
                .andExpect(jsonPath("$.data.token").value(TOKEN));

        verify(usuarioService).registrar(any(RegistroDTO.class));
    }

    @Test
    void registrar_conEmailDuplicado_devuelve409() throws Exception {
        // Arrange
        when(usuarioService.registrar(any(RegistroDTO.class)))
                .thenThrow(new com.tecmilenio.mapsconect.exception.ConflictoException(
                        "El email ya está registrado"));

        RegistroDTO body = RegistroDTO.builder()
                .email(EMAIL)
                .nombre("Juan")
                .apellido("Pérez")
                .contrasena(CONTRASENA)
                .rol("ESTUDIANTE")
                .build();

        // Act / Assert
        mockMvc.perform(post("/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("El email ya está registrado"));
    }

    @Test
    void registrar_conEmailNoInstitucional_devuelve400() throws Exception {
        // Arrange — email no pertenece a tecmilenio.mx (viola @Pattern)
        RegistroDTO body = RegistroDTO.builder()
                .email("juan@gmail.com")
                .nombre("Juan")
                .apellido("Pérez")
                .contrasena(CONTRASENA)
                .rol("ESTUDIANTE")
                .build();

        // Act / Assert
        mockMvc.perform(post("/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void registrar_conContrasenaDebil_devuelve400() throws Exception {
        // Arrange — sin mayúscula ni número (viola @Pattern de contraseña)
        RegistroDTO body = RegistroDTO.builder()
                .email(EMAIL)
                .nombre("Juan")
                .apellido("Pérez")
                .contrasena("sola")
                .rol("ESTUDIANTE")
                .build();

        // Act / Assert
        mockMvc.perform(post("/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarioService);
    }
}


