package com.mentorcore.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para registro y edición de tareas del alumno.
 * RF2, RF5
 */
@Data
public class TareaDTO {

    private Long id;

    @NotNull(message = "La fecha de registro es obligatoria")
    private LocalDate fechaRegistro;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    @NotNull(message = "Las horas dedicadas son obligatorias")
    @DecimalMin(value = "0.5", message = "El mínimo es 0.5 horas")
    private BigDecimal horasDedicadas;

    private String areaActividad;

    private String comentarioTutor;
}

