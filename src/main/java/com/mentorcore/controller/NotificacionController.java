package com.mentorcore.controller;

import com.mentorcore.model.Notificacion;
import com.mentorcore.model.Usuario;
import com.mentorcore.model.enums.RolEnum;
import com.mentorcore.service.NotificacionService;
import com.mentorcore.service.UsuarioService;
import com.mentorcore.util.ControllerMessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/**
 * Controlador genérico de notificaciones.
 * RF16
 */
@Controller
@RequestMapping("/notificaciones")
@RequiredArgsConstructor
@Slf4j
public class NotificacionController {

    private final NotificacionService notificacionService;
    private final UsuarioService usuarioService;

    @PostMapping("/{id}/leer")
    public String marcarLeida(@PathVariable Long id,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = getUsuarioAutenticado(principal);

            Notificacion notificacion = notificacionService.findById(id)
                    .orElseThrow(() -> new RuntimeException(
                            "Notificación no encontrada con id: " + id));

            if (notificacion.getReceptor() == null ||
                    !notificacion.getReceptor().getId().equals(usuario.getId())) {
                throw new RuntimeException("No puedes marcar como leída una notificación que no es tuya");
            }

            notificacionService.marcarLeida(id);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Notificación marcada como leída.");
        } catch (Exception e) {
            ControllerMessageUtil.addSafeErrorMessage(
                    redirectAttributes,
                    log,
                    "Error al marcar notificacion como leida",
                    e,
                    "No se pudo actualizar la notificación. Inténtalo de nuevo."
            );
        }

        return "redirect:" + getRutaNotificaciones(principal);
    }

    @PostMapping("/leer-todas")
    public String marcarTodasLeidas(Principal principal,
                                    RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = getUsuarioAutenticado(principal);
            notificacionService.marcarTodasLeidas(usuario);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Todas las notificaciones han sido marcadas como leídas.");
        } catch (Exception e) {
            ControllerMessageUtil.addSafeErrorMessage(
                    redirectAttributes,
                    log,
                    "Error al marcar todas las notificaciones como leidas",
                    e,
                    "No se pudieron actualizar las notificaciones. Inténtalo de nuevo."
            );
        }

        return "redirect:" + getRutaNotificaciones(principal);
    }

    private Usuario getUsuarioAutenticado(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("No hay usuario autenticado");
        }

        return usuarioService.findByNombreUsuario(principal.getName())
                .orElseThrow(() -> new RuntimeException(
                        "Usuario no encontrado: " + principal.getName()));
    }

    private String getRutaNotificaciones(Principal principal) {
        Usuario usuario = getUsuarioAutenticado(principal);

        if (usuario.getRol() == RolEnum.ALUMNO) {
            return "/alumno/notificaciones";
        }
        if (usuario.getRol() == RolEnum.TUTOR_CENTRO) {
            return "/tutor-centro/notificaciones";
        }
        if (usuario.getRol() == RolEnum.TUTOR_EMPRESA) {
            return "/tutor-empresa/notificaciones";
        }
        return "/admin/inicio";
    }
}
