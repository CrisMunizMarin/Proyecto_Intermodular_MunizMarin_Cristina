package com.mentorcore.model;

import com.mentorcore.model.enums.EstadoPeriodoEnum;
import com.mentorcore.model.enums.TipoPeriodoEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Define cada convocatoria de prácticas del departamento.
 * Puede ser ordinaria o extraordinaria, de 1º o 2º curso.
 * RF20
 */
@Entity
@Table(name = "periodo_formacion")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class PeriodoFormacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El curso académico es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_curso", nullable = false)
    private CursoAcademico cursoAcademico;

    @NotNull(message = "El tipo de periodo es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoPeriodoEnum tipo;

    @NotBlank(message = "El año académico es obligatorio")
    @Column(name = "anio_academico", nullable = false, length = 20)
    private String anioAcademico;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "horas_totales", nullable = false)
    private int horasTotales = 400;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoPeriodoEnum estado = EstadoPeriodoEnum.PLANIFICADO;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_creado_por", nullable = false)
    private Usuario creadoPor;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = EstadoPeriodoEnum.PLANIFICADO;
        }
    }

    public PeriodoFormacion(CursoAcademico cursoAcademico, TipoPeriodoEnum tipo,
                            String anioAcademico, LocalDate fechaInicio,
                            LocalDate fechaFin, Usuario creadoPor) {
        this.cursoAcademico = cursoAcademico;
        this.tipo = tipo;
        this.anioAcademico = anioAcademico;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.creadoPor = creadoPor;
    }

    public boolean isActivo() {
        return EstadoPeriodoEnum.ACTIVO.equals(this.estado);
    }

    public boolean seSolapa(LocalDate otraInicio, LocalDate otraFin) {
        return !this.fechaFin.isBefore(otraInicio) && !this.fechaInicio.isAfter(otraFin);
    }
}
