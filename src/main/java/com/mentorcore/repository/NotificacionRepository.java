package com.mentorcore.repository;

import com.mentorcore.model.Notificacion;
import com.mentorcore.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de acceso a datos para la entidad Notificacion.
 * RF13, RF16
 */
@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    // Notificaciones recibidas por un usuario (RF16)
    List<Notificacion> findByReceptorOrderByFechaEnvioDesc(Usuario receptor);

    // Notificaciones no leídas de un usuario (RF16)
    List<Notificacion> findByReceptorAndLeidaFalseOrderByFechaEnvioDesc(Usuario receptor);

    // Contar no leídas (para el badge del menú)
    long countByReceptorAndLeidaFalse(Usuario receptor);
}
