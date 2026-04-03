package com.mentorcore.model;

import com.mentorcore.model.enums.ResultadoLogEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Registro inmutable de acciones críticas para cumplimiento RGPD/LOPD.
 * Solo se insertan registros, nunca se modifican.
 * Objetivo 7
 */
@Entity
@Table(name = "log_auditoria")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class LogAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Usuario que realizó la acción. Null si fue el sistema.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @NotBlank
    @Column(name = "accion", nullable = false, length = 100)
    private String accion;

    @Column(name = "entidad_afectada", length = 50)
    private String entidadAfectada;

    @Column(name = "id_entidad")
    private Long idEntidad;

    @Column(name = "ip_origen", length = 45)
    private String ipOrigen;

    @Column(name = "datos_anteriores", columnDefinition = "JSON")
    private String datosAnteriores;

    @Column(name = "datos_nuevos", columnDefinition = "JSON")
    private String datosNuevos;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultado")
    private ResultadoLogEnum resultado;

    @PrePersist
    protected void onCreate() {
        this.fechaHora = LocalDateTime.now();
    }

    public LogAuditoria(Usuario usuario, String accion, String entidadAfectada,
                        Long idEntidad, String ipOrigen, ResultadoLogEnum resultado) {
        this.usuario = usuario;
        this.accion = accion;
        this.entidadAfectada = entidadAfectada;
        this.idEntidad = idEntidad;
        this.ipOrigen = ipOrigen;
        this.resultado = resultado;
    }
}
