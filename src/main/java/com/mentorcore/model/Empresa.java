package com.mentorcore.model;

import com.mentorcore.model.enums.EstadoEmpresaEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

/**
 * Empresa colaboradora que acoge alumnos en prácticas.
 * Una empresa puede tener N tutores de empresa y N alumnos.
 * RF18
 */
@Entity
@Table(name = "empresa")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la empresa es obligatorio")
    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @NotBlank(message = "El CIF es obligatorio")
    @Column(name = "cif", nullable = false, unique = true, length = 15)
    private String cif;

    @Column(name = "sector", length = 100)
    private String sector;

    @Column(name = "direccion", length = 255)
    private String direccion;

    @Column(name = "municipio", length = 100)
    private String municipio;

    @Column(name = "provincia", length = 100)
    private String provincia;

    @Column(name = "codigo_postal", length = 10)
    private String codigoPostal;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "email_contacto", length = 150)
    private String emailContacto;

    @Column(name = "web", length = 255)
    private String web;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoEmpresaEnum estado = EstadoEmpresaEnum.ACTIVA;

    @Column(name = "fecha_alta")
    private LocalDate fechaAlta;

    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;

    @PrePersist
    protected void onCreate() {
        if (this.fechaAlta == null) {
            this.fechaAlta = LocalDate.now();
        }
        if (this.estado == null) {
            this.estado = EstadoEmpresaEnum.ACTIVA;
        }
    }

    public Empresa(String nombre, String cif, String sector,
                   String municipio, String provincia) {
        this.nombre = nombre;
        this.cif = cif;
        this.sector = sector;
        this.municipio = municipio;
        this.provincia = provincia;
    }

    public boolean isActiva() {
        return EstadoEmpresaEnum.ACTIVA.equals(this.estado);
    }
}
