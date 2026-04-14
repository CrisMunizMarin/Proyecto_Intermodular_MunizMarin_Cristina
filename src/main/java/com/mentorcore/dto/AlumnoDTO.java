package com.mentorcore.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * DTO para creación y edición de alumnos.
 * RF10, RF11, RF13
 */
@Data
public class AlumnoDTO {

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

    @NotNull(message = "El curso académico es obligatorio")
    private Long idCursoAcademico;

    @NotNull(message = "El tutor de centro es obligatorio")
    private Long idTutorCentro;

    private String grupo;
    private String dni;
    private LocalDate fechaNacimiento;
    private String numSeguridadSocial;
    private Integer horasTotalesFe;
}

