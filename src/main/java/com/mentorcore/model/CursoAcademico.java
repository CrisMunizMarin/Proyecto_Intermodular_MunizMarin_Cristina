package com.mentorcore.model;

import com.mentorcore.model.enums.NivelEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Representa un curso académico concreto del departamento.
 * Ej: 2VIFC303 = 2º DAW Vespertino curso 2025-2026.
 * RF12, RF13, RF20
 */
@Entity
@Table(name = "curso_academico")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class CursoAcademico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Código oficial del grupo.
     * Ej: 1IFC303, 2VIFC303, 1VIFC302, 2@IFC303
     */
    @NotBlank(message = "El código de curso es obligatorio")
    @Column(name = "codigo_curso", nullable = false, unique = true, length = 15)
    private String codigoCurso;

    /**
     * Nombre descriptivo del grupo.
     * Ej: "2º DAW Vespertino"
     */
    @NotBlank(message = "El nombre del curso es obligatorio")
    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    /**
     * Ciclo formativo al que pertenece.
     * Ej: "DAW diurno", "DAW vespertino", "DAW virtual", "DAM"
     */
    @NotBlank(message = "El ciclo formativo es obligatorio")
    @Column(name = "ciclo_formativo", nullable = false, length = 100)
    private String cicloFormativo;

    /**
     * Primer o segundo curso del ciclo.
     */
    @NotNull(message = "El nivel es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "nivel", nullable = false)
    private NivelEnum nivel;

    /**
     * Año académico en formato "YYYY-YYYY".
     * Ej: "2025-2026"
     */
    @NotBlank(message = "El año académico es obligatorio")
    @Column(name = "anio_academico", nullable = false, length = 20)
    private String anioAcademico;

    /**
     * Permite desactivar cursos sin borrar su historial.
     */
    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    public CursoAcademico(String codigoCurso, String nombre, String cicloFormativo,
                          NivelEnum nivel, String anioAcademico) {
        this.codigoCurso = codigoCurso;
        this.nombre = nombre;
        this.cicloFormativo = cicloFormativo;
        this.nivel = nivel;
        this.anioAcademico = anioAcademico;
        this.activo = true;
    }

    /**
     * Devuelve el nombre completo del curso para mostrar en vistas.
     * Ej: "2º DAW Vespertino 2025-2026"
     */
    public String getNombreCompleto() {
        return this.nombre + " " + this.anioAcademico;
    }
}
