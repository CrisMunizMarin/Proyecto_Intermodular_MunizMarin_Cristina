package com.mentorcore.repository;

import com.mentorcore.model.Usuario;
import com.mentorcore.model.enums.EstadoUsuarioEnum;
import com.mentorcore.model.enums.RolEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad Usuario.
 * RF1, RF10, RF11
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Buscar por credenciales de acceso (RF1 - Login)
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);
    Optional<Usuario> findByEmail(String email);

    // Buscar por token de recuperación de contraseña (RF1)
    Optional<Usuario> findByTokenRecuperacion(String token);

    // Comprobar duplicados al crear usuario (RF10)
    boolean existsByNombreUsuario(String nombreUsuario);
    boolean existsByEmail(String email);

    // Listar por rol y estado (RF10, RF11)
    List<Usuario> findByRol(RolEnum rol);
    List<Usuario> findByEstado(EstadoUsuarioEnum estado);
    List<Usuario> findByRolAndEstado(RolEnum rol, EstadoUsuarioEnum estado);
}
