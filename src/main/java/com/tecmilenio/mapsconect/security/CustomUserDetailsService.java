package com.tecmilenio.mapsconect.security;

import com.tecmilenio.mapsconect.entity.Usuario;
import com.tecmilenio.mapsconect.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Servicio personalizado de detalles de usuario para Spring Security.
 *
 * <p>Implementa {@link UserDetailsService} y carga el {@link Usuario} desde la
 * base de datos usando el email como identificador. Construye un
 * {@link UserDetails} con el rol del usuario para que el
 * {@code AuthenticationManager} pueda autenticar credenciales.</p>
 *
 * <p>Este servicio es utilizado tanto por el proceso de login (a través del
 * {@code DaoAuthenticationProvider} configurado en {@link SecurityConfig})
 * como por el {@link JwtAuthenticationFilter} para reconstruir la
 * autenticación a partir del JWT.</p>
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    /** Repositorio de usuarios, usado para buscar por email. */
    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Carga los detalles del usuario autenticado a partir de su email.
     *
     * <p>El email es el identificador único en la plataforma (no se usa username).
     * Si el usuario no existe, lanza {@link UsernameNotFoundException} que
     * Spring Security captura y traduce en un error de autenticación 401.</p>
     *
     * @param email email del usuario (también usado como username)
     * @return {@link UserDetails} con email, contraseña encriptada y authorities
     * @throws UsernameNotFoundException si no hay ningún usuario con ese email
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        // Convierte el rol del enum a la notación Spring Security (ROLE_<ROL>)
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));

        return new User(
                usuario.getEmail(),
                usuario.getContrasena(),
                usuario.getActivo(),
                true, true, true,
                authorities);
    }
}


