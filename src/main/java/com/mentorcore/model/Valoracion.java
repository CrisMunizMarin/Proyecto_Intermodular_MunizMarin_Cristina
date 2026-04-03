package com.mentorcore.model;

import com.mentorcore.model.enums.ResultadoEnum;
import com.mentorcore.model.enums.TipoEvaluadorEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Evaluación final emitida sobre un alumno.
 * Máximo 1 valoración por alumno por tipo de evaluador.
 * RF7
 */
@Entity
@Table(name = "valoracion",
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"id_alumno", "tipo_evaluador"},
           name = "uq_valoracion_alumno_evaluador"
       ))
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Valoracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_alumno", nullable = false)
    private Alumno alumno;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_evaluador", nullable = false)
    private Usuario evaluador;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evaluador", nullable = false)
    private TipoEvaluadorEnum tipoEvaluador;

    @Column(name = "nota_global", precision = 4, scale = 2)
    private BigDecimal notaGlobal;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultado", nullable = false)
    private ResultadoEnum resultado = ResultadoEnum.PENDIENTE;

    @Min(0) @Max(10)
    @Column(name = "puntacion_actitud")
    private Integer puntacionActitud;

    @Min(0) @Max(10)
    @Column(name = "puntacion_competencias")
    private Integer puntacionCompetencias;

    @Min(0) @Max(10)
    @Column(name = "puntacion_integracion")
    private Integer puntacionIntegracion;

    @Min(0) @Max(10)
    @Column(name = "puntacion_iniciativa")
    private Integer puntacionIniciativa;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "fecha_emision", nullable = false, updatable = false)
    private LocalDateTime fechaEmision;

    /**
     * Una vez bloqueada no puede modificarse. RF7
     */
    @Column(name = "bloqueada", nullable = false)
    private boolean bloqueada = false;

    @PrePersist
    protected void onCreate() {
        this.fechaEmision = LocalDateTime.now();
    }

    public Valoracion(Alumno alumno, Usuario evaluador, TipoEvaluadorEnum tipoEvaluador) {
        this.alumno = alumno;
        this.evaluador = evaluador;
        this.tipoEvaluador = tipoEvaluador;
    }

    /**
     * Calcula la nota media de las 4 puntuaciones parciales.
     */
    public BigDecimal calcularNotaMedia() {
        int count = 0;
        int suma = 0;
        if (puntacionActitud != null)       { suma += puntacionActitud;       count++; }
        if (puntacionCompetencias != null)  { suma += puntacionCompetencias;  count++; }
        if (puntacionIntegracion != null)   { suma += puntacionIntegracion;   count++; }
        if (puntacionIniciativa != null)    { suma += puntacionIniciativa;    count++; }
        if (count == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(suma)
                .divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    public boolean isApto() {
        return ResultadoEnum.APTO.equals(this.resultado);
    }

    public void bloquear() {
        this.notaGlobal = calcularNotaMedia();
        this.bloqueada = true;
    }
}
