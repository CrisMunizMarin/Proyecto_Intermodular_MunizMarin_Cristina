package com.mentorcore.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO para el formulario de inicio de sesión.
 * RF1
 */
@Data
public class loginDTO {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    private String nombreUsuario;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}

