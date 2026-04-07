package com.mentorcore.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO con los datos necesarios para generar un informe de seguimiento.
 * RF8, RF14
 */
@Data
public class InformeDTO {

    // Datos del alumno
    private String nombreAlumno;
    private String apellidosAlumno;
    private String grupo;

    // Datos de la empresa
    private String nombreEmpresa;
    private String nombreTutorEmpresa;

    // Datos del tutor centro
    private String nombreTutorCentro;

    // Periodo
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    // Resumen de horas
    private BigDecimal horasCompletadas;
    private int horasTotales;

    // Resumen de tareas
    private long tareasValidadas;
    private long tareasPendientes;
    private long tareasRechazadas;

    // Resumen de faltas
    private long faltasJustificadas;
    private long faltasInjustificadas;

    // Valoración final
    private String resultadoTutorCentro;   // APTO / NO APTO / PENDIENTE
    private String resultadoTutorEmpresa;
    private String observacionesTutorCentro;
    private String observacionesTutorEmpresa;
}
