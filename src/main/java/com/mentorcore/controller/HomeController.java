package com.mentorcore.controller;

import com.mentorcore.model.Usuario;
import com.mentorcore.model.enums.RolEnum;
import com.mentorcore.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

/**
 * Controlador de entrada de la aplicación.
 * Redirige al login si no hay sesión y al panel correspondiente según el rol.
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UsuarioService usuarioService;

    @GetMapping("/")
    public String home(Principal principal) {
        if (principal == null) {
            return "redirect:/auth/login";
        }

        Usuario usuario = usuarioService.findByNombreUsuario(principal.getName())
                .orElseThrow(() -> new RuntimeException(
                        "Usuario no encontrado: " + principal.getName()));

        if (usuario.getRol() == RolEnum.ALUMNO) {
            return "redirect:/alumno/inicio";
        }
        if (usuario.getRol() == RolEnum.TUTOR_CENTRO) {
            return "redirect:/tutor-centro/inicio";
        }
        if (usuario.getRol() == RolEnum.TUTOR_EMPRESA) {
            return "redirect:/tutor-empresa/inicio";
        }
        if (usuario.getRol() == RolEnum.ADMIN) {
            return "redirect:/admin/inicio";
        }

        return "redirect:/auth/login";
    }
}
