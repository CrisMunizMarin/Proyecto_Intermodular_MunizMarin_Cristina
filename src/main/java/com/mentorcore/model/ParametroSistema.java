package com.mentorcore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Parámetros globales del sistema configurables por el Administrador.
 * Permite ajustar valores sin recompilar la aplicación.
 * RF12
 */
@Entity
@Table(name = "parametro_sistema")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class ParametroSistema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La clave es obligatoria")
    @Column(name = "clave", nullable = false, unique = true, length = 100)
    private String clave;

    @NotBlank(message = "El valor es obligatorio")
    @Column(name = "valor", nullable = false, columnDefinition = "TEXT")
    private String valor;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "tipo_dato", length = 20)
    private String tipoDato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_modificado_por")
    private Usuario modificadoPor;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    public ParametroSistema(String clave, String valor, String descripcion, String tipoDato) {
        this.clave = clave;
        this.valor = valor;
        this.descripcion = descripcion;
        this.tipoDato = tipoDato;
    }

    public int getValorAsInt() {
        return Integer.parseInt(this.valor.trim());
    }

    public boolean getValorAsBoolean() {
        return Boolean.parseBoolean(this.valor.trim());
    }

    public void actualizar(String nuevoValor, Usuario admin) {
        this.valor = nuevoValor;
        this.modificadoPor = admin;
        this.fechaModificacion = LocalDateTime.now();
    }
}



