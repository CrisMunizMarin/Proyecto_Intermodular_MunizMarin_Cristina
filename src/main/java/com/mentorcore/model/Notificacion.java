package com.mentorcore.model;

import com.mentorcore.model.enums.TipoNotificacionEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Sistema de mensajería interna entre roles.
 * Soporta avisos manuales y alertas automáticas.
 * RF13, RF16
 */
@Entity
@Table(name = "notificacion")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_emisor")
    private Usuario emisor;

    @jakarta.validation.constraints.NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_receptor", nullable = false)
    private Usuario receptor;

    @jakarta.validation.constraints.NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoNotificacionEnum tipo;

    @NotBlank(message = "El título es obligatorio")
    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @NotBlank(message = "El mensaje es obligatorio")
    @Column(name = "mensaje", nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Column(name = "leida", nullable = false)
    private boolean leida = false;

    @Column(name = "fecha_envio", nullable = false, updatable = false)
    private LocalDateTime fechaEnvio;

    @Column(name = "fecha_lectura")
    private LocalDateTime fechaLectura;

    @Column(name = "entidad_relacionada", length = 50)
    private String entidadRelacionada;

    @Column(name = "id_entidad_relacionada")
    private Long idEntidadRelacionada;

    @PrePersist
    protected void onCreate() {
        this.fechaEnvio = LocalDateTime.now();
    }

    public Notificacion(Usuario emisor, Usuario receptor,
                        TipoNotificacionEnum tipo, String titulo, String mensaje) {
        this.emisor = emisor;
        this.receptor = receptor;
        this.tipo = tipo;
        this.titulo = titulo;
        this.mensaje = mensaje;
    }

    public void marcarLeida() {
        this.leida = true;
        this.fechaLectura = LocalDateTime.now();
    }

    public String getResumen() {
        if (mensaje == null) return "";
        return mensaje.length() > 80 ? mensaje.substring(0, 80) + "..." : mensaje;
    }
}
