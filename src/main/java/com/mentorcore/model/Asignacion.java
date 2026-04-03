package com.mentorcore.model;

import com.mentorcore.model.enums.EstadoFeEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Historial de la relación alumno-empresa-tutorEmpresa en un periodo concreto.
 * Permite la reasignación conservando registros anteriores como FINALIZADA.
 * Solo puede existir 1 asignación ACTIVA por alumno en cada momento.
 * RF13, RF21
 */
@Entity
@Table(name = "asignacion")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Asignacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El alumno es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_alumno", nullable = false)
    private Alumno alumno;

    @NotNull(message = "La empresa es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empresa", nullable = false)
    private Empresa empresa;

    @NotNull(message = "El tutor de empresa es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tutor_empresa", nullable = false)
    private TutorEmpresa tutorEmpresa;

    @NotNull(message = "El periodo de formación es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_periodo", nullable = false)
    private PeriodoFormacion periodo;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    /**
     * Null si la asignación sigue activa.
     */
    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoFeEnum estado = EstadoFeEnum.EN_CURSO;

    /**
     * Motivo por el que se reasignó al alumno a otra empresa. RF21
     */
    @Column(name = "motivo_cambio", columnDefinition = "TEXT")
    private String motivoCambio;

    /**
     * Usuario que realizó la reasignación (Admin o TutorCentro). RF21
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_reasignado_por")
    private Usuario reasignadoPor;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = EstadoFeEnum.EN_CURSO;
        }
    }

    public Asignacion(Alumno alumno, Empresa empresa, TutorEmpresa tutorEmpresa,
                      PeriodoFormacion periodo, LocalDate fechaInicio) {
        this.alumno = alumno;
        this.empresa = empresa;
        this.tutorEmpresa = tutorEmpresa;
        this.periodo = periodo;
        this.fechaInicio = fechaInicio;
    }

    public boolean isActiva() {
        return EstadoFeEnum.EN_CURSO.equals(this.estado);
    }

    /**
     * Cierra la asignación actual con un motivo. RF21
     */
    public void finalizar(String motivo) {
        this.estado = EstadoFeEnum.FINALIZADO;
        this.fechaFin = LocalDate.now();
        this.motivoCambio = motivo;
    }
}
