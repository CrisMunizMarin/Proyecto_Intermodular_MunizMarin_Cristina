package com.mentorcore.model;

import com.mentorcore.model.enums.EstadoFaltaEnum;
import com.mentorcore.model.enums.TipoFaltaEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Falta de asistencia registrada por el Tutor Empresa.
 * El alumno puede adjuntar un justificante para su revisión.
 * RF19, RF22
 */
@Entity
@Table(name = "falta_asistencia",
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"id_alumno", "fecha_falta"},
           name = "uq_falta_alumno_fecha"
       ))
@Getter
@Setter
@NoArgsConstructor
@ToString
public class FaltaAsistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_alumno", nullable = false)
    private Alumno alumno;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_asignacion", nullable = false)
    private Asignacion asignacion;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_registrado_por", nullable = false)
    private TutorEmpresa registradoPor;

    @NotNull(message = "La fecha de la falta es obligatoria")
    @Column(name = "fecha_falta", nullable = false)
    private LocalDate fechaFalta;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoFaltaEnum tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoFaltaEnum estado = EstadoFaltaEnum.INJUSTIFICADA;

    @Column(name = "observacion", columnDefinition = "TEXT")
    private String observacion;

    /**
     * Documento justificante adjuntado por el alumno. RF22
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_justificante")
    private Documento justificante;

    /**
     * Tutor Centro que validó el justificante. RF22
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_validado_por")
    private TutorCentro validadoPor;

    @Column(name = "fecha_validacion")
    private LocalDateTime fechaValidacion;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = EstadoFaltaEnum.INJUSTIFICADA;
        }
    }

    public FaltaAsistencia(Alumno alumno, Asignacion asignacion,
                           TutorEmpresa registradoPor, LocalDate fechaFalta,
                           TipoFaltaEnum tipo) {
        this.alumno = alumno;
        this.asignacion = asignacion;
        this.registradoPor = registradoPor;
        this.fechaFalta = fechaFalta;
        this.tipo = tipo;
    }

    /**
     * Alumno adjunta justificante para solicitar revisión. RF22
     */
    public void adjuntarJustificante(Documento doc) {
        this.justificante = doc;
        this.estado = EstadoFaltaEnum.PENDIENTE_REVISION;
    }

    /**
     * Tutor Centro aprueba el justificante. RF22
     */
    public void aprobarJustificante(TutorCentro tutor) {
        this.estado = EstadoFaltaEnum.JUSTIFICADA;
        this.validadoPor = tutor;
        this.fechaValidacion = LocalDateTime.now();
    }

    /**
     * Tutor Centro deniega el justificante. RF22
     */
    public void denegarJustificante(TutorCentro tutor) {
        this.estado = EstadoFaltaEnum.INJUSTIFICADA;
        this.justificante = null;
        this.validadoPor = tutor;
        this.fechaValidacion = LocalDateTime.now();
    }
}
