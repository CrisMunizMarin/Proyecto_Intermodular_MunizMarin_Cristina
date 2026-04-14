package com.mentorcore.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO para mostrar el progreso del alumno en la FE.
 * RF2, RF8
 */
@Data
public class ProgresoDTO {

    private Long idAlumno;

    private BigDecimal horasCompletadas;
    private int horasTotalesFe;
    private BigDecimal horasRestantes;
    private double porcentajeCompletado;

    private long tareasValidadas;
    private long tareasPendientes;
    private long tareasRechazadas;

    private long faltasJustificadas;
    private long faltasInjustificadas;

    private boolean haCompletadoHoras;
}

