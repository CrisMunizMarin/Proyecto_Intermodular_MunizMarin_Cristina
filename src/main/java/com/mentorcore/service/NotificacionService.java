package com.mentorcore.service;

import com.mentorcore.model.Notificacion;
import com.mentorcore.model.Usuario;
import com.mentorcore.model.enums.TipoNotificacionEnum;
import com.mentorcore.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de gestión de notificaciones internas.
 * RF13, RF16
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;


    // BÚSQUEDAS

    @Transactional(readOnly = true)
    public Optional<Notificacion> findById(Long id) {
        return notificacionRepository.findById(id);
    }

    /**
     * Devuelve todas las notificaciones recibidas por un usuario. RF16
     */
    @Transactional(readOnly = true)
    public List<Notificacion> findByReceptor(Usuario receptor) {
        return notificacionRepository
                .findByReceptorOrderByFechaEnvioDesc(receptor);
    }

    /**
     * Devuelve solo las notificaciones no leídas. RF16
     * Usada para mostrar el listado de avisos pendientes.
     */
    @Transactional(readOnly = true)
    public List<Notificacion> findNoLeidasByReceptor(Usuario receptor) {
        return notificacionRepository
                .findByReceptorAndLeidaFalseOrderByFechaEnvioDesc(receptor);
    }

    /**
     * Cuenta las notificaciones no leídas para el badge del menú. RF16
     */
    @Transactional(readOnly = true)
    public long contarNoLeidas(Usuario receptor) {
        return notificacionRepository.countByReceptorAndLeidaFalse(receptor);
    }


    //ENVÍO DE NOTIFICACIONES (RF13, RF16)

    /**
     * Envía una notificación interna entre dos usuarios. RF13
     *
     * @param emisor    usuario que genera el aviso
     * @param receptor  usuario destinatario
     * @param tipo      tipo de notificación
     * @param titulo    asunto breve
     * @param mensaje   cuerpo del mensaje
     */
    @Transactional
    public Notificacion enviar(Usuario emisor, Usuario receptor,
                                TipoNotificacionEnum tipo,
                                String titulo, String mensaje) {

        Notificacion notif = new Notificacion();
        notif.setEmisor(emisor);
        notif.setReceptor(receptor);
        notif.setTipo(tipo);
        notif.setTitulo(titulo);
        notif.setMensaje(mensaje);
        notif.setLeida(false);
        notif.setFechaEnvio(LocalDateTime.now());

        Notificacion guardada = notificacionRepository.save(notif);
        log.info("Notificación [{}] enviada de '{}' a '{}': {}",
                tipo, emisor.getNombreUsuario(),
                receptor.getNombreUsuario(), titulo);
        return guardada;
    }

    /**
     * Envía un aviso automático del sistema (sin emisor humano). RF16
     * Usado para alertas de inactividad, fechas próximas, etc.
     *
     * @param receptor usuario destinatario
     * @param tipo     tipo de notificación
     * @param titulo   asunto breve
     * @param mensaje  cuerpo del mensaje
     */
    @Transactional
    public Notificacion enviarSistema(Usuario receptor,
                                       TipoNotificacionEnum tipo,
                                       String titulo, String mensaje) {

        Notificacion notif = new Notificacion();
        notif.setEmisor(null);   // null = generada por el sistema
        notif.setReceptor(receptor);
        notif.setTipo(tipo);
        notif.setTitulo(titulo);
        notif.setMensaje(mensaje);
        notif.setLeida(false);
        notif.setFechaEnvio(LocalDateTime.now());

        Notificacion guardada = notificacionRepository.save(notif);
        log.info("Notificación del sistema [{}] enviada a '{}': {}",
                tipo, receptor.getNombreUsuario(), titulo);
        return guardada;
    }


    // MÉTODOS DE CONVENIENCIA (RF16)
    // Evitan repetir la lógica de título/mensaje en los controladores

    /**
     * Notifica al tutor centro que hay una nueva tarea pendiente de revisión.
     */
    @Transactional
    public void notificarNuevaTarea(Usuario tutorCentro, String nombreAlumno) {
        enviarSistema(tutorCentro,
                TipoNotificacionEnum.AVISO,
                "Nueva tarea pendiente",
                "El alumno " + nombreAlumno + " ha registrado una nueva tarea.");
    }

    /**
     * Notifica al alumno que su tarea ha sido validada o rechazada.
     */
    @Transactional
    public void notificarRevisionTarea(Usuario alumno, boolean validada,
                                        String comentario) {
        String estado  = validada ? "validada ✓" : "rechazada ✗";
        String titulo  = validada ? "Tarea validada" : "Tarea rechazada";
        String mensaje = "Tu tarea ha sido " + estado + ". " +
                         (comentario != null ? "Comentario: " + comentario : "");
        enviarSistema(alumno, TipoNotificacionEnum.VALIDACION, titulo, mensaje);
    }

    /**
     * Notifica al alumno y al tutor centro el registro de una falta.
     */
    @Transactional
    public void notificarFaltaAsistencia(Usuario alumno, Usuario tutorCentro,
                                          String fecha, String tipo) {
        String mensaje = "Se ha registrado una falta " + tipo + " el " + fecha + ".";
        enviarSistema(alumno,      TipoNotificacionEnum.ALERTA, "Falta de asistencia", mensaje);
        enviarSistema(tutorCentro, TipoNotificacionEnum.ALERTA, "Falta de asistencia", mensaje);
    }

    /**
     * Notifica al tutor centro que un alumno ha adjuntado un justificante.
     */
    @Transactional
    public void notificarJustificante(Usuario tutorCentro, String nombreAlumno,
                                       String fechaFalta) {
        enviarSistema(tutorCentro,
                TipoNotificacionEnum.AVISO,
                "Justificante pendiente de revisión",
                "El alumno " + nombreAlumno +
                " ha adjuntado un justificante para la falta del " + fechaFalta + ".");
    }

    /**
     * Notifica al alumno y al nuevo tutor empresa una reasignación. RF21
     */
    @Transactional
    public void notificarReasignacion(Usuario alumno, Usuario nuevoTutorEmpresa,
                                       String nombreEmpresa) {
        String mensaje = "Has sido reasignado/a a la empresa: " + nombreEmpresa + ".";
        enviarSistema(alumno,           TipoNotificacionEnum.AVISO, "Reasignación de empresa", mensaje);
        enviarSistema(nuevoTutorEmpresa, TipoNotificacionEnum.AVISO, "Nuevo alumno asignado",
                "Se te ha asignado un nuevo alumno en " + nombreEmpresa + ".");
    }


    //MARCAR COMO LEÍDA 

    /**
     * Marca una notificación como leída. RF16
     */
    @Transactional
    public void marcarLeida(Long idNotificacion) {
        notificacionRepository.findById(idNotificacion).ifPresent(n -> {
            n.setLeida(true);
            n.setFechaLectura(LocalDateTime.now());
            notificacionRepository.save(n);
        });
    }

    /**
     * Marca todas las notificaciones de un usuario como leídas. RF16
     */
    @Transactional
    public void marcarTodasLeidas(Usuario receptor) {
        List<Notificacion> noLeidas = findNoLeidasByReceptor(receptor);
        noLeidas.forEach(n -> {
            n.setLeida(true);
            n.setFechaLectura(LocalDateTime.now());
        });
        notificacionRepository.saveAll(noLeidas);
        log.info("Marcadas {} notificaciones como leídas para '{}'",
                noLeidas.size(), receptor.getNombreUsuario());
    }
}
