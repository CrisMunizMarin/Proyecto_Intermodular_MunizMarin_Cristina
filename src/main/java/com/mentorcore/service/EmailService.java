package com.mentorcore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Servicio de envío de emails.
 * En desarrollo usa MailHog (puerto 1025) para simular el envío.
 * RF1 (recuperación de contraseña), RF16 (alertas y notificaciones)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    // Remitente por defecto
    private static final String FROM = "noreply@mentorcore.es";


    //EMAIL DE RECUPERACIÓN DE CONTRASEÑA (RF1)

    /**
     * Envía el email con el enlace de recuperación de contraseña. RF1
     * Se ejecuta de forma asíncrona para no bloquear la petición HTTP.
     *
     * @param emailDestino email del usuario que solicita el reset
     * @param token        token UUID generado por UsuarioService
     */
    @Async
    public void enviarRecuperacionPassword(String emailDestino, String token) {
        String enlace = "http://localhost:8080/auth/reset-password?token=" + token;

        String cuerpo = """
                Hola,
                
                Has solicitado restablecer tu contraseña en MentorCore.
                
                Haz clic en el siguiente enlace para crear una nueva contraseña:
                %s
                
                Este enlace caduca en 24 horas.
                
                Si no has solicitado este cambio, ignora este mensaje.
                
                El equipo de MentorCore
                """.formatted(enlace);

        enviarSimple(emailDestino, "Recuperación de contraseña — MentorCore", cuerpo);
    }


    // EMAIL DE BIENVENIDA (RF10)

    /**
     * Envía las credenciales iniciales al nuevo usuario creado por el Admin. RF10
     *
     * @param emailDestino    email del nuevo usuario
     * @param nombreUsuario   nombre de usuario asignado
     * @param passwordTemporal contraseña temporal en texto plano (se cifra después)
     */
    @Async
    public void enviarBienvenida(String emailDestino, String nombreUsuario,
                                  String passwordTemporal) {
        String cuerpo = """
                Bienvenido/a a MentorCore,
                
                Tu cuenta ha sido creada. Aquí están tus credenciales de acceso:
                
                  Usuario:    %s
                  Contraseña: %s
                
                Por seguridad, te recomendamos cambiar tu contraseña en el primer acceso.
                
                Accede a la plataforma en: http://localhost:8080/login
                
                El equipo de MentorCore
                """.formatted(nombreUsuario, passwordTemporal);

        enviarSimple(emailDestino, "Bienvenido/a a MentorCore — Tus credenciales", cuerpo);
    }


    //EMAILS DE NOTIFICACIÓN (RF16)

    /**
     * Notifica al tutor centro que un alumno ha registrado una nueva tarea. RF16
     */
    @Async
    public void notificarNuevaTarea(String emailTutor, String nombreAlumno,
                                     String fechaTarea) {
        String cuerpo = """
                Hola,
                
                El alumno %s ha registrado una nueva tarea el %s.
                
                Accede a MentorCore para revisar y validar la tarea:
                http://localhost:8080/tutor-centro/tareas
                
                El equipo de MentorCore
                """.formatted(nombreAlumno, fechaTarea);

        enviarSimple(emailTutor,
                "Nueva tarea registrada — " + nombreAlumno, cuerpo);
    }

    /**
     * Notifica al alumno que su tarea ha sido validada o rechazada. RF16
     *
     * @param validada true = validada, false = rechazada
     */
    @Async
    public void notificarRevisionTarea(String emailAlumno, String fechaTarea,
                                        boolean validada, String comentario) {
        String estado = validada ? "VALIDADA ✓" : "RECHAZADA ✗";
        String cuerpo = """
                Hola,
                
                Tu tarea del %s ha sido %s.
                
                Comentario del tutor: %s
                
                Accede a MentorCore para ver el detalle:
                http://localhost:8080/alumno/tareas
                
                El equipo de MentorCore
                """.formatted(fechaTarea, estado, comentario != null ? comentario : "—");

        enviarSimple(emailAlumno,
                "Revisión de tarea — " + estado, cuerpo);
    }

    /**
     * Notifica al alumno que se ha registrado una falta de asistencia. RF16, RF18
     */
    @Async
    public void notificarFaltaAsistencia(String emailAlumno, String fecha,
                                          String tipo, String observacion) {
        String cuerpo = """
                Hola,
                
                Se ha registrado una falta de asistencia en tu expediente:
                
                  Fecha: %s
                  Tipo:  %s
                  Observación: %s
                
                Si dispones de justificante, adjúntalo en MentorCore:
                http://localhost:8080/alumno/faltas
                
                El equipo de MentorCore
                """.formatted(fecha, tipo, observacion != null ? observacion : "—");

        enviarSimple(emailAlumno,
                "Falta de asistencia registrada — " + fecha, cuerpo);
    }

    /**
     * Notifica al tutor centro que un alumno ha adjuntado un justificante. RF22
     */
    @Async
    public void notificarJustificante(String emailTutor, String nombreAlumno,
                                       String fechaFalta) {
        String cuerpo = """
                Hola,
                
                El alumno %s ha adjuntado un justificante para la falta del %s.
                
                Accede a MentorCore para revisarlo:
                http://localhost:8080/tutor-centro/faltas
                
                El equipo de MentorCore
                """.formatted(nombreAlumno, fechaFalta);

        enviarSimple(emailTutor,
                "Justificante pendiente de revisión — " + nombreAlumno, cuerpo);
    }

    /**
     * Notifica al alumno y al nuevo tutor empresa una reasignación. RF21
     */
    @Async
    public void notificarReasignacion(String emailDestinatario,
                                       String nombreAlumno,
                                       String nuevaEmpresa) {
        String cuerpo = """
                Hola,
                
                Se ha realizado una reasignación en MentorCore:
                
                  Alumno:      %s
                  Nueva empresa: %s
                
                Accede a MentorCore para ver los detalles:
                http://localhost:8080/login
                
                El equipo de MentorCore
                """.formatted(nombreAlumno, nuevaEmpresa);

        enviarSimple(emailDestinatario,
                "Reasignación de empresa — " + nombreAlumno, cuerpo);
    }


    //MÉTODO HTML (para futuras mejoras)

    /**
     * Envía un email con cuerpo HTML. Reservado para futuras plantillas. RF16
     */
    @Async
    public void enviarHtml(String emailDestino, String asunto, String cuerpoHtml) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");
            helper.setFrom(FROM);
            helper.setTo(emailDestino);
            helper.setSubject(asunto);
            helper.setText(cuerpoHtml, true);
            mailSender.send(mensaje);
            log.info("Email HTML enviado a '{}'  — Asunto: '{}'", emailDestino, asunto);
        } catch (MessagingException e) {
            log.error("Error enviando email HTML a '{}': {}", emailDestino, e.getMessage());
        }
    }


    //HELPERS PRIVADOS

    /**
     * Envía un email de texto plano. Base de todos los métodos anteriores.
     */
    private void enviarSimple(String emailDestino, String asunto, String cuerpo) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(FROM);
            mensaje.setTo(emailDestino);
            mensaje.setSubject(asunto);
            mensaje.setText(cuerpo);
            mailSender.send(mensaje);
            log.info("Email enviado a '{}' — Asunto: '{}'", emailDestino, asunto);
        } catch (Exception e) {
            log.error("Error enviando email a '{}': {}", emailDestino, e.getMessage());
        }
    }
}
