package com.mentorcore.service;

import com.mentorcore.model.Usuario;
import com.mentorcore.model.enums.EstadoUsuarioEnum;
import com.mentorcore.model.enums.RolEnum;
import com.mentorcore.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio de gestión de usuarios.
 * Implementa UserDetailsService para la integración con Spring Security.
 * RF1, RF10, RF11
 */
@Service
@RequiredArgsConstructor  // Lombok genera constructor con todos los campos final
@Slf4j                    // Lombok genera el logger
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;


    // SPRING SECURITY — UserDetailsService (RF1)


    /**
     * Spring Security llama a este método al procesar el formulario de login.
     * Busca al usuario por nombre de usuario y construye el UserDetails.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String nombreUsuario)
            throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> {
                    log.warn("Intento de login fallido: usuario '{}' no encontrado", nombreUsuario);
                    return new UsernameNotFoundException(
                            "Usuario no encontrado: " + nombreUsuario);
                });

        // Verificar que el usuario está activo
        if (!usuario.isActivo()) {
            log.warn("Intento de login de usuario inactivo: '{}'", nombreUsuario);
            throw new UsernameNotFoundException("Usuario inactivo o suspendido");
        }

        // Spring Security necesita el rol con prefijo ROLE_
        String rol = "ROLE_" + usuario.getRol().name();

        return User.builder()
                .username(usuario.getNombreUsuario())
                .password(usuario.getPasswordHash())
                .authorities(new SimpleGrantedAuthority(rol))
                .build();
    }


    // BÚSQUEDAS (RF10, RF11)

    @Transactional(readOnly = true)
    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> findByNombreUsuario(String nombreUsuario) {
        return usuarioRepository.findByNombreUsuario(nombreUsuario);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Usuario> findByRol(RolEnum rol) {
        return usuarioRepository.findByRol(rol);
    }


    // CREACIÓN Y MODIFICACIÓN (RF10, RF11)

    /**
     * Guarda un usuario nuevo cifrando su contraseña con BCrypt.
     */
    @Transactional
    public Usuario guardar(Usuario usuario) {
        // Cifrar la contraseña si viene en texto plano
        if (!usuario.getPasswordHash().startsWith("$2a$")) {
            usuario.setPasswordHash(
                    passwordEncoder.encode(usuario.getPasswordHash()));
        }
        return usuarioRepository.save(usuario);
    }

    /**
     * Actualiza los datos básicos de un usuario. RF11
     */
    @Transactional
    public Usuario actualizar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    /**
     * Suspende una cuenta de usuario. RF10
     */
    @Transactional
    public void suspender(Long id) {
        usuarioRepository.findById(id).ifPresent(u -> {
            u.setEstado(EstadoUsuarioEnum.SUSPENDIDO);
            usuarioRepository.save(u);
            log.info("Usuario '{}' suspendido", u.getNombreUsuario());
        });
    }

    /**
     * Reactiva una cuenta suspendida. RF10
     */
    @Transactional
    public void activar(Long id) {
        usuarioRepository.findById(id).ifPresent(u -> {
            u.setEstado(EstadoUsuarioEnum.ACTIVO);
            usuarioRepository.save(u);
            log.info("Usuario '{}' reactivado", u.getNombreUsuario());
        });
    }

    /**
     * Elimina un usuario del sistema. RF10
     * En producción considerar anonimización por RGPD.
     */
    @Transactional
    public void eliminar(Long id) {
        usuarioRepository.deleteById(id);
        log.info("Usuario con id {} eliminado", id);
    }

   
    // VALIDACIONES (RF10)

    public boolean existeNombreUsuario(String nombreUsuario) {
        return usuarioRepository.existsByNombreUsuario(nombreUsuario);
    }

    public boolean existeEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

   
    // RECUPERACIÓN DE CONTRASEÑA (RF1)

    /**
     * Genera un token UUID de recuperación con 24h de validez.
     */
    @Transactional
    public String generarTokenRecuperacion(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(
                        "No existe ningún usuario con ese email"));

        String token = UUID.randomUUID().toString();
        usuario.setTokenRecuperacion(token);
        usuario.setTokenExpira(LocalDateTime.now().plusHours(24));
        usuarioRepository.save(usuario);

        log.info("Token de recuperación generado para '{}'", email);
        return token;
    }

    /**
     * Restablece la contraseña si el token es válido.
     */
    @Transactional
    public boolean restablecerPassword(String token, String nuevaPassword) {
        Optional<Usuario> opt = usuarioRepository.findByTokenRecuperacion(token);

        if (opt.isEmpty()) {
            log.warn("Intento de reset con token inválido");
            return false;
        }

        Usuario usuario = opt.get();

        if (!usuario.isTokenValido()) {
            log.warn("Token de recuperación caducado para '{}'",
                    usuario.getNombreUsuario());
            return false;
        }

        usuario.setPasswordHash(passwordEncoder.encode(nuevaPassword));
        usuario.invalidarToken();
        usuarioRepository.save(usuario);

        log.info("Contraseña restablecida para '{}'", usuario.getNombreUsuario());
        return true;
    }

    /**
     * Registra el momento del último acceso tras el login. RF1
     */
    @Transactional
    public void registrarLogin(String nombreUsuario) {
        usuarioRepository.findByNombreUsuario(nombreUsuario).ifPresent(u -> {
            u.registrarLogin();
            usuarioRepository.save(u);
        });
    }
}
