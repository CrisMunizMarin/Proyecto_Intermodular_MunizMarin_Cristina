package com.mentorcore.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO para subida de documentos del alumno.
 * RF3, RF22
 */
@Data
public class DocumentoDTO {

    private Long id;

    @NotNull(message = "El tipo de documento es obligatorio")
    private Long idTipoDocumento;

    @NotNull(message = "Debes seleccionar un archivo")
    private MultipartFile archivo;

    private String comentarioRevision;
}

