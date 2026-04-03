package com.mentorcore.model;

import com.mentorcore.model.enums.EstadoConvenioEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

/**
 * Documento legal que formaliza la relación alumno-empresa-centro.
 * Ciclo de vida: BORRADOR → FIRMADO → VIGENTE → FINALIZADO
 * RF3
 */
@Entity
@Table(name = "convenio")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Convenio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_alumno", nullable = false)
    private Alumno alumno;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empresa", nullable = false)
    private Empresa empresa;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tutor_centro", nullable = false)
    private TutorCentro tutorCentro;

    @NotBlank(message = "El número de convenio es obligatorio")
    @Column(name = "numero_convenio", nullable = false, unique = true, length = 50)
    private String numeroConvenio;

    @Column(name = "fecha_firma")
    private LocalDate fechaFirma;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "horas_semanales")
    private Integer horasSemanales;

    @Column(name = "horario_descripcion", columnDefinition = "TEXT")
    private String horarioDescripcion;

    @Column(name = "actividades_previstas", columnDefinition = "TEXT")
    private String actividadesPrevistas;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoConvenioEnum estado = EstadoConvenioEnum.BORRADOR;

    @Column(name = "archivo_pdf_url", length = 500)
    private String archivoPdfUrl;

    public Convenio(Alumno alumno, Empresa empresa, TutorCentro tutorCentro,
                    String numeroConvenio, LocalDate fechaInicio, LocalDate fechaFin) {
        this.alumno = alumno;
        this.empresa = empresa;
        this.tutorCentro = tutorCentro;
        this.numeroConvenio = numeroConvenio;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public void firmar() {
        this.estado = EstadoConvenioEnum.FIRMADO;
        this.fechaFirma = LocalDate.now();
    }

    public void activar() {
        this.estado = EstadoConvenioEnum.VIGENTE;
    }

    public void finalizar() {
        this.estado = EstadoConvenioEnum.FINALIZADO;
    }

    public void anular() {
        this.estado = EstadoConvenioEnum.ANULADO;
    }

    public boolean isVigente() {
        return EstadoConvenioEnum.VIGENTE.equals(this.estado);
    }
}
