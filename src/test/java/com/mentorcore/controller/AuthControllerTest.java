package com.mentorcore.controller;

import com.mentorcore.service.EmailService;
import com.mentorcore.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.mentorcore.exception.GlobalExceptionHandler.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private EmailService emailService;

    @Test
    void recuperarPassword_enviaEmailYRedirigeConMensajeGenerico() throws Exception {
        when(usuarioService.generarTokenRecuperacion("alumno@mentorcore.es"))
                .thenReturn("token-demo");
        doNothing().when(emailService).enviarRecuperacionPassword("alumno@mentorcore.es", "token-demo");

        mockMvc.perform(post("/auth/recuperar-password")
                        .param("email", "alumno@mentorcore.es"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/recuperar-password"))
                .andExpect(flash().attribute("successMsg",
                        "Si el email existe en el sistema, recibirás un enlace de recuperación."));

        verify(usuarioService).generarTokenRecuperacion("alumno@mentorcore.es");
        verify(emailService).enviarRecuperacionPassword("alumno@mentorcore.es", "token-demo");
    }

    @Test
    void resetPassword_conContrasenasDistintas_vuelveAlFormulario() throws Exception {
        mockMvc.perform(post("/auth/reset-password")
                        .param("token", "abc")
                        .param("password", "Password123")
                        .param("confirmar", "Password456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/reset-password?token=abc"))
                .andExpect(flash().attribute("errorMsg", "Las contraseñas no coinciden."));
    }

    @Test
    void resetPassword_conTokenValido_redirigeALogin() throws Exception {
        when(usuarioService.restablecerPassword(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(post("/auth/reset-password")
                        .param("token", "abc")
                        .param("password", "Password123")
                        .param("confirmar", "Password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"))
                .andExpect(flash().attribute("successMsg",
                        "Contraseña restablecida correctamente. Ya puedes iniciar sesión."));

        verify(usuarioService).restablecerPassword("abc", "Password123");
    }
}
