package com.mentorcore.dto;

import com.mentorcore.model.enums.TipoFaltaEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * DTO para registro y gestión de faltas de asistencia.
 * RF19, RF22
 */
@Data
public class FaltaAsistenciaDTO {

    private Long id;

    @NotNull(message = "La fecha de la falta es obligatoria")
    private LocalDate fechaFalta;

    @NotNull(message = "El tipo de falta es obligatorio")
    private TipoFaltaEnum tipo;

    private String observacion;

    private String motivoDenegacion;
}

