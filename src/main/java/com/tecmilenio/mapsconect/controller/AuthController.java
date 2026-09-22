package com.tecmilenio.mapsconect.controller;

import com.tecmilenio.mapsconect.dto.ApiResponse;
import com.tecmilenio.mapsconect.dto.LoginDTO;
import com.tecmilenio.mapsconect.dto.OnboardingEstudianteDTO;
import com.tecmilenio.mapsconect.dto.OnboardingProfesorDTO;
import com.tecmilenio.mapsconect.dto.RegistroDTO;
import com.tecmilenio.mapsconect.dto.TokenDTO;
import com.tecmilenio.mapsconect.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador de autenticación y gestión de perfil de usuario.
 *
 * <p>Expone los endpoints públicos de registro e inicio de sesión, así como
 * los endpoints protegidos para consultar el estado del perfil y completar
 * el onboarding (registro de datos académicos) de estudiantes y profesores.</p>
 *
 * <p>Flujo típico:</p>
 * <ol>
 *   <li><b>POST /auth/registrar</b>: crea la cuenta y devuelve un JWT.</li>
 *   <li><b>POST /auth/login</b>: autentica y devuelve un JWT.</li>
 *   <li><b>GET /auth/perfil-estado</b>: verifica si el onboarding está completo.</li>
 *   <li><b>POST /auth/onboarding/*</b>: registra datos académicos.</li>
 * </ol>
 *
 * <p>El login está protegido con rate limiting (5 intentos / 15 min por IP+email)
 * mediante {@link com.tecmilenio.mapsconect.security.LoginRateLimiter}.</p>
 *
 * @see UsuarioService
 */
@Tag(name = "Autenticación", description = "Registro, login y estado de perfil")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @Operation(
            summary = "Registrar nuevo usuario",
            description = "Crea una cuenta con email institucional (@tecmilenio.mx) y devuelve un token JWT."
    )
    @PostMapping("/registrar")
    public ResponseEntity<ApiResponse<TokenDTO>> registrar(@Valid @RequestBody RegistroDTO registroDTO) {
        TokenDTO token = usuarioService.registrar(registroDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(token, "Usuario registrado correctamente"));
    }

    @Operation(
            summary = "Iniciar sesión",
            description = "Autentica con email y contraseña, devuelve un token JWT. " +
                    "Rate limited a 5 intentos / 15 min por IP+email."
    )
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenDTO>> login(@Valid @RequestBody LoginDTO loginDTO,
            jakarta.servlet.http.HttpServletRequest request) {
        TokenDTO token = usuarioService.login(loginDTO, request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(token, "Sesión iniciada correctamente"));
    }

    @Operation(
            summary = "Estado del perfil",
            description = "Verifica si el usuario autenticado ha completado el onboarding. " +
                    "Requiere token JWT válido."
    )
    @GetMapping("/perfil-estado")
    public ResponseEntity<ApiResponse<Map<String, Object>>> estadoPerfil(Authentication authentication) {
        Map<String, Object> estado = usuarioService.estadoPerfil(emailDeSesion(authentication));
        return ResponseEntity.ok(ApiResponse.success(estado, "Estado del perfil obtenido"));
    }

    @Operation(
            summary = "Completar onboarding de estudiante",
            description = "Registra la información académica de un estudiante (matrícula, carrera, semestre, materias)."
    )
    @PostMapping("/onboarding/estudiante")
    public ResponseEntity<ApiResponse<Void>> completarOnboardingEstudiante(
            @Valid @RequestBody OnboardingEstudianteDTO dto,
            Authentication authentication) {

        usuarioService.completarOnboardingEstudiante(emailDeSesion(authentication), dto);
        return ResponseEntity.ok(ApiResponse.success(null, "Perfil de estudiante completado"));
    }

    @Operation(
            summary = "Completar onboarding de profesor",
            description = "Registra la información académica de un docente (nómina, especialidad, horario, materias)."
    )
    @PostMapping("/onboarding/profesor")
    public ResponseEntity<ApiResponse<Void>> completarOnboardingProfesor(
            @Valid @RequestBody OnboardingProfesorDTO dto,
            Authentication authentication) {

        usuarioService.completarOnboardingProfesor(emailDeSesion(authentication), dto);
        return ResponseEntity.ok(ApiResponse.success(null, "Perfil de docente completado"));
    }

    private String emailDeSesion(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("No se encontró una sesión activa");
        }
        return authentication.getName();
    }

}