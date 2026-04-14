package com.mentorcore.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO para creación y edición de tutores de empresa.
 * RF13, RF19
 */
@Data
public class TutorEmpresaDTO {

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

    @NotNull(message = "La empresa es obligatoria")
    private Long idEmpresa;

    private String cargo;
    private String departamentoEmpresa;
}
