package com.mentorcore.model;

import com.mentorcore.model.enums.ContextoDocumentoEnum;
import com.mentorcore.model.enums.EstadoDocumentoEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Archivo subido al expediente digital del alumno.
 * Puede ser un documento del expediente o un justificante de falta.
 * RF3, RF6, RF22
 */
@Entity
@Table(name = "documento")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El alumno es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_alumno", nullable = false)
    private Alumno alumno;

    @NotNull(message = "El tipo de documento es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_documento", nullable = false)
    private TipoDocumento tipoDocumento;

    @NotNull(message = "El usuario que sube el documento es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_subido_por", nullable = false)
    private Usuario subidoPor;

    @NotBlank(message = "El nombre del archivo es obligatorio")
    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @NotBlank(message = "La ruta de almacenamiento es obligatoria")
    @Column(name = "ruta_almacenamiento", nullable = false, length = 500)
    private String rutaAlmacenamiento;

    @Column(name = "tamano_bytes")
    private Long tamanoBytes;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoDocumentoEnum estado = EstadoDocumentoEnum.PENDIENTE;

    @Column(name = "comentario_revision", columnDefinition = "TEXT")
    private String comentarioRevision;

    /**
     * Distingue si es un documento del expediente o un justificante de falta.
     * RF22
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "contexto", nullable = false)
    private ContextoDocumentoEnum contexto = ContextoDocumentoEnum.PERSONAL_ALUMNO;

    @Column(name = "es_obligatorio", nullable = false)
    private boolean esObligatorio = false;

    @Column(name = "fecha_subida", nullable = false, updatable = false)
    private LocalDateTime fechaSubida;

    @Column(name = "fecha_revision")
    private LocalDateTime fechaRevision;

    @PrePersist
    protected void onCreate() {
        this.fechaSubida = LocalDateTime.now();
    }

    public Documento(Alumno alumno, TipoDocumento tipoDocumento, Usuario subidoPor,
                     String nombreArchivo, String rutaAlmacenamiento, String mimeType) {
        this.alumno = alumno;
        this.tipoDocumento = tipoDocumento;
        this.subidoPor = subidoPor;
        this.nombreArchivo = nombreArchivo;
        this.rutaAlmacenamiento = rutaAlmacenamiento;
        this.mimeType = mimeType;
    }

    public void validar(String comentario) {
        this.estado = EstadoDocumentoEnum.VALIDADO;
        this.comentarioRevision = comentario;
        this.fechaRevision = LocalDateTime.now();
    }

    public void rechazar(String motivo) {
        this.estado = EstadoDocumentoEnum.RECHAZADO;
        this.comentarioRevision = motivo;
        this.fechaRevision = LocalDateTime.now();
    }

    public String getExtension() {
        if (nombreArchivo != null && nombreArchivo.contains(".")) {
            return nombreArchivo.substring(nombreArchivo.lastIndexOf(".") + 1).toLowerCase();
        }
        return "";
    }

    public boolean esJustificante() {
        return ContextoDocumentoEnum.JUSTIFICANTE_FALTA.equals(this.contexto);
    }
}
