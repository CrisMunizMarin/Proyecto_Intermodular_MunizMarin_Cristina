package com.mentorcore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Catálogo configurable de tipos de documentos.
 * Gestionado por el Administrador. RF12
 */
@Entity
@Table(name = "tipo_documento")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class TipoDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del tipo de documento es obligatorio")
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "es_obligatorio", nullable = false)
    private boolean esObligatorio = false;

    @Column(name = "rol_responsable", length = 20)
    private String rolResponsable;

    @Column(name = "extensiones_permitidas", length = 100)
    private String extensionesPermitidas;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    public TipoDocumento(String nombre, String descripcion,
                         boolean esObligatorio, String extensionesPermitidas) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.esObligatorio = esObligatorio;
        this.extensionesPermitidas = extensionesPermitidas;
    }

    public boolean isExtensionValida(String extension) {
        if (extensionesPermitidas == null || extensionesPermitidas.isBlank()) return true;
        String ext = extension.toLowerCase().trim();
        for (String permitida : extensionesPermitidas.split(",")) {
            if (permitida.trim().equals(ext)) return true;
        }
        return false;
    }
}
