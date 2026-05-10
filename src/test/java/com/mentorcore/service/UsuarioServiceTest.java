package com.mentorcore.service;

import com.mentorcore.model.Usuario;
import com.mentorcore.model.enums.EstadoUsuarioEnum;
import com.mentorcore.model.enums.RolEnum;
import com.mentorcore.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombreUsuario("alumno1");
        usuario.setEmail("alumno1@mentorcore.es");
        usuario.setPasswordHash("$2a$hash");
        usuario.setRol(RolEnum.ALUMNO);
        usuario.setEstado(EstadoUsuarioEnum.ACTIVO);
    }

    @Test
    void generarTokenRecuperacion_guardaTokenYCaducidad() {
        when(usuarioRepository.findByEmail("alumno1@mentorcore.es")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String token = usuarioService.generarTokenRecuperacion("alumno1@mentorcore.es");

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario guardado = captor.getValue();

        assertThat(token).isNotBlank();
        assertThat(guardado.getTokenRecuperacion()).isEqualTo(token);
        assertThat(guardado.getTokenExpira()).isAfter(LocalDateTime.now().plusHours(23));
    }

    @Test
    void restablecerPassword_conTokenValido_cambiaPasswordEInvalidaToken() {
        usuario.setTokenRecuperacion("token-ok");
        usuario.setTokenExpira(LocalDateTime.now().plusHours(1));

        when(usuarioRepository.findByTokenRecuperacion("token-ok")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("NuevaPass123")).thenReturn("$2a$nuevohash");

        boolean resultado = usuarioService.restablecerPassword("token-ok", "NuevaPass123");

        assertTrue(resultado);
        assertThat(usuario.getPasswordHash()).isEqualTo("$2a$nuevohash");
        assertThat(usuario.getTokenRecuperacion()).isNull();
        assertThat(usuario.getTokenExpira()).isNull();
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void restablecerPassword_conTokenInvalido_devuelveFalse() {
        when(usuarioRepository.findByTokenRecuperacion("token-mal")).thenReturn(Optional.empty());

        boolean resultado = usuarioService.restablecerPassword("token-mal", "NuevaPass123");

        assertFalse(resultado);
    }
}
