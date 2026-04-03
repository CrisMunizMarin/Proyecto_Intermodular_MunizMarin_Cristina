package com.mentorcore.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Perfil extendido del usuario con rol TUTOR_CENTRO.
 * Docente responsable de la supervisión académica de sus alumnos.
 * RF4, RF5, RF6, RF7, RF8, RF13, RF18, RF19, RF20, RF21
 */
@Entity
@Table(name = "tutor_centro")
@PrimaryKeyJoinColumn(name = "id_usuario")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class TutorCentro extends Usuario {

    // DATOS PROFESIONALES
    /**
     * Departamento docente al que pertenece el tutor.
     * Ej: "Informática y Comunicaciones"
     */
    @Column(name = "departamento", length = 100)
    private String departamento;

    /**
     * Módulo o especialidad que imparte.
     * Ej: "Desarrollo de Aplicaciones Web"
     */
    @Column(name = "especialidad", length = 100)
    private String especialidad;

    /**
     * Número de expediente del docente en el centro educativo.
     */
    @Column(name = "num_expediente_docente", length = 30)
    private String numExpedienteDocente;


    // CONSTRUCTOR

    public TutorCentro(String nombreUsuario, String email, String passwordHash,
                       String nombre, String apellidos,
                       String departamento, String especialidad) {
        super(nombreUsuario, email, passwordHash, nombre, apellidos,
                com.mentorcore.model.enums.RolEnum.TUTOR_CENTRO);
        this.departamento = departamento;
        this.especialidad = especialidad;
    }
}
