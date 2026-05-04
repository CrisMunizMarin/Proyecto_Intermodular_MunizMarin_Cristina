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

    @Column(name = "motivo_justificacion", columnDefinition = "TEXT")
    private String motivoJustificacion;

    @Column(name = "horas_ausencia", precision = 4, scale = 2)
    private java.math.BigDecimal horasAusencia;

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

    @Column(name = "comentario_verificacion_empresa", columnDefinition = "TEXT")
    private String comentarioVerificacionEmpresa;

    @Column(name = "fecha_verificacion_empresa")
    private LocalDateTime fechaVerificacionEmpresa;

    @Column(name = "motivo_denegacion", columnDefinition = "TEXT")
    private String motivoDenegacion;

    @Column(name = "comentario_revision_centro", columnDefinition = "TEXT")
    private String comentarioRevisionCentro;

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
    public void adjuntarJustificante(Documento doc, String motivoJustificacion,
                                     java.math.BigDecimal horasAusencia) {
        this.justificante = doc;
        this.motivoJustificacion = motivoJustificacion;
        this.horasAusencia = horasAusencia;
        this.estado = EstadoFaltaEnum.PENDIENTE_REVISION;
        this.comentarioVerificacionEmpresa = null;
        this.fechaVerificacionEmpresa = null;
        this.motivoDenegacion = null;
    }

    public void verificarPorEmpresa(String comentarioVerificacionEmpresa) {
        this.estado = EstadoFaltaEnum.VERIFICADA_EMPRESA;
        this.comentarioVerificacionEmpresa = comentarioVerificacionEmpresa;
        this.fechaVerificacionEmpresa = LocalDateTime.now();
    }

    /**
     * Tutor Centro aprueba el justificante. RF22
     */
    public void aprobarJustificante(TutorCentro tutor, String comentarioRevisionCentro) {
        this.estado = EstadoFaltaEnum.JUSTIFICADA;
        this.validadoPor = tutor;
        this.fechaValidacion = LocalDateTime.now();
        this.motivoDenegacion = null;
        this.comentarioRevisionCentro = comentarioRevisionCentro;
    }

    /**
     * Tutor Centro deniega el justificante. RF22
     */
    public void denegarJustificante(TutorCentro tutor, String motivoDenegacion) {
        this.estado = EstadoFaltaEnum.INJUSTIFICADA;
        this.justificante = null;
        this.validadoPor = tutor;
        this.fechaValidacion = LocalDateTime.now();
        this.motivoDenegacion = motivoDenegacion;
        this.comentarioRevisionCentro = motivoDenegacion;
        this.comentarioVerificacionEmpresa = null;
        this.fechaVerificacionEmpresa = null;
    }
}
