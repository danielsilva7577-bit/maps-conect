package com.tecmilenio.mapsconect.service;

import com.tecmilenio.mapsconect.dto.LoginDTO;
import com.tecmilenio.mapsconect.dto.OnboardingEstudianteDTO;
import com.tecmilenio.mapsconect.dto.OnboardingProfesorDTO;
import com.tecmilenio.mapsconect.dto.RegistroDTO;
import com.tecmilenio.mapsconect.dto.TokenDTO;
import com.tecmilenio.mapsconect.dto.UsuarioDTO;
import com.tecmilenio.mapsconect.entity.Estudiante;
import com.tecmilenio.mapsconect.entity.EstudianteMateria;
import com.tecmilenio.mapsconect.entity.Profesor;
import com.tecmilenio.mapsconect.entity.ProfesorCertificado;
import com.tecmilenio.mapsconect.entity.ProfesorMateria;
import com.tecmilenio.mapsconect.entity.Usuario;
import com.tecmilenio.mapsconect.exception.ResourceNotFoundException;
import com.tecmilenio.mapsconect.repository.EstudianteMateriaRepository;
import com.tecmilenio.mapsconect.repository.EstudianteRepository;
import com.tecmilenio.mapsconect.repository.ProfesorCertificadoRepository;
import com.tecmilenio.mapsconect.repository.ProfesorMateriaRepository;
import com.tecmilenio.mapsconect.repository.ProfesorRepository;
import com.tecmilenio.mapsconect.repository.UsuarioRepository;
import com.tecmilenio.mapsconect.security.JwtTokenProvider;
import com.tecmilenio.mapsconect.security.LoginRateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio principal de usuarios.
 *
 * <p>Gestiona el registro de nuevas cuentas (validando unicidad de email),
 * el inicio de sesión (con rate limiting de intentos fallidos), la verificación
 * del estado de onboarding, y la finalización del perfil académmico tanto
 * para estudiantes como para profesores.</p>
 *
 * <p>Al registrar o iniciar sesión, genera un token JWT mediante
 * {@link JwtTokenProvider}. El rate limiting de login se delega a
 * {@link LoginRateLimiter}.</p>
 *
 * @see JwtTokenProvider
 * @see LoginRateLimiter
 */
@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private LoginRateLimiter loginRateLimiter;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private ProfesorRepository profesorRepository;

    @Autowired
    private ProfesorMateriaRepository profesorMateriaRepository;

    @Autowired
    private ProfesorCertificadoRepository profesorCertificadoRepository;

    @Autowired
    private EstudianteMateriaRepository estudianteMateriaRepository;

    @Transactional
    public TokenDTO registrar(RegistroDTO registroDTO) {
        if (usuarioRepository.existsByEmail(registroDTO.getEmail())) {
            throw new com.tecmilenio.mapsconect.exception.ConflictoException("El email ya está registrado");
        }

        Usuario.Rol rol = resolverRol(registroDTO.getRol());

        if (rol == Usuario.Rol.PROFESOR && (registroDTO.getNumeroNomina() == null
                || registroDTO.getNumeroNomina().trim().isEmpty())) {
            throw new IllegalArgumentException("El número de nómina es obligatorio para docentes");
        }

        Usuario usuario = Usuario.builder()
                .email(registroDTO.getEmail())
                .nombreCompleto(registroDTO.getNombre() + " " + registroDTO.getApellido())
                .contrasena(passwordEncoder.encode(registroDTO.getContrasena()))
                .rol(rol)
                .activo(true)
                .build();

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        if (rol == Usuario.Rol.PROFESOR && !profesorRepository.existsByNumeroNomina(
                registroDTO.getNumeroNomina().trim())) {
            profesorRepository.save(Profesor.builder()
                    .idUsuario(usuarioGuardado.getId())
                    .numeroNomina(registroDTO.getNumeroNomina().trim())
                    .disponibleChat(true)
                    .build());
        }

        String token = jwtTokenProvider.generateToken(usuarioGuardado.getEmail());

        return TokenDTO.builder()
                .token(token)
                .tipo("Bearer")
                .expiresIn(jwtTokenProvider.getExpiresInSeconds())
                .usuario(mapearADTO(usuarioGuardado))
                .build();
    }

    public TokenDTO login(LoginDTO loginDTO, String ipCliente) {
        String clave = (ipCliente == null || ipCliente.isBlank())
                ? "EMAIL:" + loginDTO.getEmail().toLowerCase()
                : ipCliente;

        Long bloqueado = loginRateLimiter.tiempoRestanteBloqueo(clave);
        if (bloqueado != null) {
            throw new IllegalStateException(
                    "Demasiados intentos fallidos. Intenta de nuevo en " + bloqueado + " segundos.");
        }

        Usuario usuario = usuarioRepository.findByEmail(loginDTO.getEmail()).orElse(null);
        boolean valido = usuario != null
                && passwordEncoder.matches(loginDTO.getContrasena(),
                        usuario == null ? "" : usuario.getContrasena());

        if (!valido) {
            loginRateLimiter.registrarFallo(clave);
            dormirUnPoco();
            throw new BadCredentialsException("Credenciales incorrectas");
        }

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new BadCredentialsException("Tu cuenta ha sido inhabilitada. Contacta al administrador.");
        }

        loginRateLimiter.registrarExito(clave);
        String token = jwtTokenProvider.generateToken(usuario.getEmail());

        return TokenDTO.builder()
                .token(token)
                .tipo("Bearer")
                .expiresIn(jwtTokenProvider.getExpiresInSeconds())
                .usuario(mapearADTO(usuario))
                .build();
    }

    private void dormirUnPoco() {
        try {
            Thread.sleep(600);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    @Transactional
    public void completarOnboardingEstudiante(String email, OnboardingEstudianteDTO dto) {
        Usuario usuario = obtenerUsuarioPorEmail(email);
        if (usuario.getRol() != Usuario.Rol.ESTUDIANTE) {
            throw new IllegalArgumentException("Solo los estudiantes pueden completar este perfil");
        }
        if (dto.getSemestre() < 1 || dto.getSemestre() > 12) {
            throw new IllegalArgumentException("El semestre debe estar entre 1 y 12");
        }

        Estudiante existente = estudianteRepository.findByIdUsuario(usuario.getId()).orElse(null);
        String matricula = dto.getMatricula().trim();

        if (estudianteRepository.existsByMatricula(matricula)
                && (existente == null || !matricula.equals(existente.getMatricula()))) {
            throw new IllegalArgumentException("La matrícula ya está registrada");
        }

        Estudiante estudiante;
        if (existente != null) {
            existente.setMatricula(matricula);
            existente.setIdCarrera(dto.getIdCarrera());
            existente.setSemestreActual(dto.getSemestre());
            estudiante = estudianteRepository.save(existente);
        } else {
            estudiante = estudianteRepository.save(Estudiante.builder()
                    .idUsuario(usuario.getId())
                    .matricula(matricula)
                    .idCarrera(dto.getIdCarrera())
                    .semestreActual(dto.getSemestre())
                    .build());
        }

        estudianteMateriaRepository.deleteByIdEstudiante(estudiante.getId());
        Integer idEstudiante = estudiante.getId();
        List<Integer> materias = dto.getMaterias() == null ? List.of() : dto.getMaterias();
        for (Integer idMateria : materias) {
            if (!estudianteMateriaRepository.existsByIdEstudianteAndIdMateria(idEstudiante, idMateria)) {
                estudianteMateriaRepository.save(EstudianteMateria.builder()
                        .idEstudiante(idEstudiante)
                        .idMateria(idMateria)
                        .build());
            }
        }
    }

    @Transactional
    public void completarOnboardingProfesor(String email, OnboardingProfesorDTO dto) {
        Usuario usuario = obtenerUsuarioPorEmail(email);
        if (usuario.getRol() != Usuario.Rol.PROFESOR) {
            throw new IllegalArgumentException("Solo los docentes pueden completar este perfil");
        }

        String nomina = dto.getNumeroNomina().trim();
        Profesor profesor = profesorRepository.findByIdUsuario(usuario.getId()).orElse(null);

        boolean nominaEnUso = profesorRepository.existsByNumeroNomina(nomina)
                && (profesor == null || !nomina.equals(profesor.getNumeroNomina()));
        if (nominaEnUso) {
            throw new IllegalArgumentException("El número de nómina ya está registrado");
        }

        if (profesor == null) {
            profesor = Profesor.builder()
                    .idUsuario(usuario.getId())
                    .numeroNomina(nomina)
                    .disponibleChat(true)
                    .build();
        }

        profesor.setNumeroNomina(nomina);
        profesor.setAreaEspecialidad(dto.getAreaEspecialidad());
        profesor.setSemestresAsignados(dto.getSemestresAsignados());
        profesor.setHorarioAsesorias(dto.getHorarioAsesorias());
        profesor.setEnlaceSalaVirtual(dto.getEnlaceSalaVirtual());
        profesor.setBiografia(dto.getSemblanza());
        Profesor profesorGuardado = profesorRepository.save(profesor);

        profesorMateriaRepository.deleteByIdProfesor(profesorGuardado.getId());
        String ciclo = dto.getSemestresAsignados() == null || dto.getSemestresAsignados().trim().isEmpty()
                ? "CICLO-ACTUAL" : dto.getSemestresAsignados().trim();
        for (Integer idMateria : dto.getMaterias()) {
            if (!profesorMateriaRepository.existsByIdProfesorAndIdMateria(
                    profesorGuardado.getId(), idMateria)) {
                profesorMateriaRepository.save(ProfesorMateria.builder()
                        .idProfesor(profesorGuardado.getId())
                        .idMateria(idMateria)
                        .cicloAcademico(ciclo)
                        .build());
            }
        }

        profesorCertificadoRepository.deleteByIdProfesor(profesorGuardado.getId());
        for (Integer idCertificado : dto.getCertificados()) {
            if (!profesorCertificadoRepository.existsByIdProfesorAndIdCertificado(
                    profesorGuardado.getId(), idCertificado)) {
                profesorCertificadoRepository.save(ProfesorCertificado.builder()
                        .idProfesor(profesorGuardado.getId())
                        .idCertificado(idCertificado)
                        .build());
            }
        }
    }

    public Map<String, Object> estadoPerfil(String email) {
        Usuario usuario = obtenerUsuarioPorEmail(email);

        boolean completado = false;
        if (usuario.getRol() == Usuario.Rol.ESTUDIANTE) {
            completado = estudianteRepository.existsByIdUsuario(usuario.getId());
        } else if (usuario.getRol() == Usuario.Rol.PROFESOR) {
            Profesor profesor = profesorRepository.findByIdUsuario(usuario.getId()).orElse(null);
            completado = profesor != null
                    && profesor.getAreaEspecialidad() != null
                    && !profesor.getAreaEspecialidad().isEmpty();
        } else if (usuario.getRol() == Usuario.Rol.ADMINISTRADOR) {
            // El administrador no pasa por onboarding (no es alumno ni docente),
            // por lo que su perfil siempre se considera completo.
            completado = true;
        }

        Map<String, Object> estado = new HashMap<>();
        estado.put("completado", completado);
        estado.put("rol", usuario.getRol().name());
        return estado;
    }

    public UsuarioDTO obtenerPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return mapearADTO(usuario);
    }

    public Usuario obtenerUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private Usuario.Rol resolverRol(String rol) {
        if (rol == null || rol.trim().isEmpty()) {
            return Usuario.Rol.ESTUDIANTE;
        }
        try {
            return Usuario.Rol.valueOf(rol.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return Usuario.Rol.ESTUDIANTE;
        }
    }

    private UsuarioDTO mapearADTO(Usuario usuario) {
        return UsuarioDTO.builder()
                .id(usuario.getId())
                .email(usuario.getEmail())
                .nombre(usuario.getNombreCompleto())
                .rol(usuario.getRol().toString())
                .activo(usuario.getActivo())
                .foto(usuario.getFotoUrl())
                .build();
    }

}