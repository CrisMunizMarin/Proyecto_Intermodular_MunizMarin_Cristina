package com.mentorcore.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Perfil extendido del usuario con rol ALUMNO.
 * Contiene los datos académicos y de seguimiento de la FE.
 * RF2, RF3, RF13 - RNF5 (datos sensibles RGPD)
 */
@Entity
@Table(name = "alumno")
@PrimaryKeyJoinColumn(name = "id_usuario")  // FK que une con la tabla usuario
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class Alumno extends Usuario {

    // RELACIONES CON OTRAS ENTIDADES

    /**
     * Curso académico al que pertenece el alumno.
     * Un alumno pertenece a exactamente 1 curso. RF13
     */
    @NotNull(message = "El curso académico es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_curso", nullable = false)
    private CursoAcademico cursoAcademico;

    /**
     * Tutor del centro educativo asignado al alumno.
     * Un alumno tiene exactamente 1 tutor centro. RF13
     */
    @NotNull(message = "El tutor del centro es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tutor_centro", nullable = false)
    private TutorCentro tutorCentro;


    // DATOS ACADÉMICOS

    @Column(name = "grupo", length = 20)
    private String grupo;


    // DATOS PERSONALES SENSIBLES (RNF5 - RGPD)

    @Column(name = "dni", unique = true, length = 15)
    private String dni;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "num_seguridad_social", length = 20)
    private String numSeguridadSocial;


    // SEGUIMIENTO DE HORAS FE (RF2)
    /**
     * Total de horas de FE requeridas para completar las prácticas.
     * Por defecto 400h según la normativa vigente.
     */
    @Column(name = "horas_totales_fe", nullable = false)
    private int horasTotalesFe = 400;

    /**
     * Horas acumuladas validadas por el tutor.
     * Se incrementa automáticamente al validar cada tarea diaria.
     */
    @Column(name = "horas_completadas", nullable = false, precision = 6, scale = 2)
    private BigDecimal horasCompletadas = BigDecimal.ZERO;


    // CONSTRUCTOR

    public Alumno(String nombreUsuario, String email, String passwordHash,
                  String nombre, String apellidos,
                  CursoAcademico cursoAcademico, TutorCentro tutorCentro, String grupo) {
        super(nombreUsuario, email, passwordHash, nombre, apellidos,
                com.mentorcore.model.enums.RolEnum.ALUMNO);
        this.cursoAcademico = cursoAcademico;
        this.tutorCentro = tutorCentro;
        this.grupo = grupo;
    }


    // MÉTODOS DE NEGOCIO (RF2)

    /**
     * Añade horas al contador tras validar una tarea diaria.
     * Solo se llama desde TareaService cuando el tutor valida una tarea.
     */
    public void acumularHoras(BigDecimal horas) {
        if (horas != null && horas.compareTo(BigDecimal.ZERO) > 0) {
            this.horasCompletadas = this.horasCompletadas.add(horas);
        }
    }

    /**
     * Calcula el porcentaje de horas completadas sobre el total requerido.
     * Devuelve un valor entre 0.0 y 100.0
     */
    public double calcularPorcentajeCompletado() {
        if (horasTotalesFe == 0) return 0.0;
        return horasCompletadas
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(horasTotalesFe), 2, java.math.RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * Devuelve las horas que le quedan al alumno para completar la FE.
     */
    public BigDecimal getHorasRestantes() {
        BigDecimal restantes = BigDecimal.valueOf(horasTotalesFe).subtract(horasCompletadas);
        return restantes.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : restantes;
    }

    /**
     * Comprueba si el alumno ha completado todas las horas requeridas.
     */
    public boolean haCompletadoHoras() {
        return horasCompletadas.compareTo(BigDecimal.valueOf(horasTotalesFe)) >= 0;
    }
}
