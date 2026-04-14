package com.mentorcore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * DTO para creación y edición de convenios.
 * RF3, RF6
 */
@Data
public class ConvenioDTO {

    private Long id;

    @NotNull(message = "El alumno es obligatorio")
    private Long idAlumno;

    @NotNull(message = "La empresa es obligatoria")
    private Long idEmpresa;

    @NotNull(message = "El tutor de centro es obligatorio")
    private Long idTutorCentro;

    @NotBlank(message = "El número de convenio es obligatorio")
    private String numeroConvenio;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate fechaFin;

    private Integer horasSemanales;
    private String horarioDescripcion;
    private String actividadesPrevistas;
}

