package com.mentorcore.controller;

import com.mentorcore.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador de autenticación.
 * Gestiona login, logout y recuperación de contraseña.
 * RF1
 */
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UsuarioService usuarioService;


    // LOGIN (RF1)
    /**
     * Muestra el formulario de login.
     * Spring Security procesa el POST automáticamente en /auth/login.
     */
    @GetMapping("/login")
    public String login(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        if (error != null) {
            model.addAttribute("errorMsg",
                    "Usuario o contraseña incorrectos. Inténtalo de nuevo.");
        }
        if (logout != null) {
            model.addAttribute("logoutMsg",
                    "Has cerrado sesión correctamente.");
        }

        return "auth/login";  //  templates/auth/login.html
    }


    // RECUPERACIÓN DE CONTRASEÑA (RF1)


    /**
     * Muestra el formulario para solicitar el email de recuperación.
     */
    @GetMapping("/recuperar-password")
    public String mostrarFormRecuperacion() {
        return "auth/password";  //  templates/auth/password.html
    }

    /**
     * Procesa la solicitud de recuperación: genera token y envía email.
     */
    @PostMapping("/recuperar-password")
    public String procesarRecuperacion(
            @RequestParam("email") String email,
            RedirectAttributes redirectAttributes) {
        try {
            String token = usuarioService.generarTokenRecuperacion(email);

            // TODO: Llamar a EmailService para enviar el email con el token
            // emailService.enviarEmailRecuperacion(email, token);
            log.info("Token generado para {}: {}", email, token);

            redirectAttributes.addFlashAttribute("successMsg",
                    "Si el email existe en el sistema, recibirás un enlace de recuperación.");

        } catch (RuntimeException e) {
            // No revelamos si el email existe o no por seguridad
            redirectAttributes.addFlashAttribute("successMsg",
                    "Si el email existe en el sistema, recibirás un enlace de recuperación.");
        }

        return "redirect:/auth/recuperar-password";
    }

    /**
     * Muestra el formulario para introducir la nueva contraseña.
     */
    @GetMapping("/reset-password")
    public String mostrarFormReset(
            @RequestParam("token") String token,
            Model model) {
        model.addAttribute("token", token);
        return "auth/recuperar-password";  //  templates/auth/recuperar-password.html
    }

    /**
     * Procesa el cambio de contraseña con el token recibido por email.
     */
    @PostMapping("/reset-password")
    public String procesarReset(
            @RequestParam("token") String token,
            @RequestParam("password") String password,
            @RequestParam("confirmar") String confirmar,
            RedirectAttributes redirectAttributes) {

        if (!password.equals(confirmar)) {
            redirectAttributes.addFlashAttribute("errorMsg",
                    "Las contraseñas no coinciden.");
            return "redirect:/auth/reset-password?token=" + token;
        }

        if (password.length() < 8) {
            redirectAttributes.addFlashAttribute("errorMsg",
                    "La contraseña debe tener al menos 8 caracteres.");
            return "redirect:/auth/reset-password?token=" + token;
        }

        boolean ok = usuarioService.restablecerPassword(token, password);

        if (ok) {
            redirectAttributes.addFlashAttribute("successMsg",
                    "Contraseña restablecida correctamente. Ya puedes iniciar sesión.");
            return "redirect:/auth/login";
        } else {
            redirectAttributes.addFlashAttribute("errorMsg",
                    "El enlace ha caducado o no es válido. Solicita uno nuevo.");
            return "redirect:/auth/recuperar-password";
        }
    }
}
