package com.mentorcore.dto;

import com.mentorcore.model.enums.RolEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO para creación y edición básica de usuarios.
 * RF10, RF11
 */
@Data
public class UsuarioDTO {

    private Long id;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    private String nombreUsuario;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String email;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "Los apellidos son obligatorios")
    private String apellidos;

    private String telefono;

    private String password;

    @NotNull(message = "El rol es obligatorio")
    private RolEnum rol;
}

