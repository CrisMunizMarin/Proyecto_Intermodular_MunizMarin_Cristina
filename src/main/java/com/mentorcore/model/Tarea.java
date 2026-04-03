package com.mentorcore.model;

import com.mentorcore.model.enums.EstadoValidacionEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Registro diario de actividades del alumno durante la FE.
 * Cada registro representa un día de trabajo con descripción y horas.
 * RF2, RF5, RF9
 */
@Entity
@Table(name = "tarea")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Tarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El alumno es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_alumno", nullable = false)
    private Alumno alumno;

    @NotNull(message = "La fecha es obligatoria")
    @Column(name = "fecha_registro", nullable = false)
    private LocalDate fechaRegistro;

    @NotBlank(message = "La descripción de la tarea es obligatoria")
    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @NotNull(message = "Las horas dedicadas son obligatorias")
    @DecimalMin(value = "0.5", message = "El mínimo es 0.5 horas")
    @Column(name = "horas_dedicadas", nullable = false, precision = 4, scale = 2)
    private BigDecimal horasDedicadas;

    @Column(name = "area_actividad", length = 100)
    private String areaActividad;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_validacion", nullable = false)
    private EstadoValidacionEnum estadoValidacion = EstadoValidacionEnum.PENDIENTE;

    /**
     * Comentario del tutor al validar o rechazar la tarea. RF5
     */
    @Column(name = "comentario_tutor", columnDefinition = "TEXT")
    private String comentarioTutor;

    /**
     * Tutor que validó o rechazó la tarea. RF5
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_validador")
    private Usuario validador;

    @Column(name = "fecha_validacion")
    private LocalDateTime fechaValidacion;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.estadoValidacion == null) {
            this.estadoValidacion = EstadoValidacionEnum.PENDIENTE;
        }
    }

    public Tarea(Alumno alumno, LocalDate fechaRegistro,
                 String descripcion, BigDecimal horasDedicadas) {
        this.alumno = alumno;
        this.fechaRegistro = fechaRegistro;
        this.descripcion = descripcion;
        this.horasDedicadas = horasDedicadas;
    }

    public boolean isPendiente() {
        return EstadoValidacionEnum.PENDIENTE.equals(this.estadoValidacion);
    }

    /**
     * Valida la tarea y acumula las horas al alumno. RF5
     */
    public void validar(Usuario tutor, String comentario) {
        this.estadoValidacion = EstadoValidacionEnum.VALIDADO;
        this.validador = tutor;
        this.comentarioTutor = comentario;
        this.fechaValidacion = LocalDateTime.now();
        this.alumno.acumularHoras(this.horasDedicadas);
    }

    /**
     * Rechaza la tarea con un motivo. RF5
     */
    public void rechazar(Usuario tutor, String motivo) {
        this.estadoValidacion = EstadoValidacionEnum.RECHAZADO;
        this.validador = tutor;
        this.comentarioTutor = motivo;
        this.fechaValidacion = LocalDateTime.now();
    }
}
