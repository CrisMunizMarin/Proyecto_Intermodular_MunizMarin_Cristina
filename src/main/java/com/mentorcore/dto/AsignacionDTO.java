package com.mentorcore.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * DTO para creación, reasignación y cierre de asignaciones.
 * RF13, RF21
 */
@Data
public class AsignacionDTO {

    private Long id;

    @NotNull(message = "El alumno es obligatorio")
    private Long idAlumno;

    @NotNull(message = "La empresa es obligatoria")
    private Long idEmpresa;

    @NotNull(message = "El tutor de empresa es obligatorio")
    private Long idTutorEmpresa;

    @NotNull(message = "El periodo es obligatorio")
    private Long idPeriodo;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    private String motivoCambio;
}

