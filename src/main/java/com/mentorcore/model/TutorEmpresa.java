package com.mentorcore.model;

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
 * Perfil extendido del usuario con rol TUTOR_EMPRESA.
 * Responsable de la supervisión presencial del alumno en la empresa.
 * Puede tener N alumnos asignados.
 * RF6, RF7, RF9, RF13, RF19
 */
@Entity
@Table(name = "tutor_empresa")
@PrimaryKeyJoinColumn(name = "id_usuario")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class TutorEmpresa extends Usuario {


    // RELACIÓN CON EMPRESA
    /**
     * Empresa a la que pertenece este tutor.
     * Un tutor empresa pertenece a exactamente 1 empresa.
     * Una empresa puede tener N tutores. RF18
     */
    @NotNull(message = "La empresa es obligatoria para el tutor de empresa")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empresa", nullable = false)
    private Empresa empresa;

    // DATOS PROFESIONALES

    /**
     * Cargo o puesto del tutor dentro de la empresa.
     * Ej: "Jefe de Desarrollo", "Técnico Senior"
     */
    @Column(name = "cargo", length = 100)
    private String cargo;

    /**
     * Departamento de la empresa donde trabaja el tutor.
     * Ej: "Departamento de Tecnología"
     */
    @Column(name = "departamento_empresa", length = 100)
    private String departamentoEmpresa;

    // CONSTRUCTOR

    public TutorEmpresa(String nombreUsuario, String email, String passwordHash,
                        String nombre, String apellidos,
                        Empresa empresa, String cargo) {
        super(nombreUsuario, email, passwordHash, nombre, apellidos,
                com.mentorcore.model.enums.RolEnum.TUTOR_EMPRESA);
        this.empresa = empresa;
        this.cargo = cargo;
    }
}
