package com.mentorcore.dto;

import com.mentorcore.model.enums.ResultadoEnum;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * DTO para emisión y edición de valoraciones finales.
 * RF7
 */
@Data
public class ValoracionDTO {

    private Long id;

    @Min(value = 0, message = "La puntuación mínima es 0")
    @Max(value = 10, message = "La puntuación máxima es 10")
    private Integer actitud;

    @Min(value = 0, message = "La puntuación mínima es 0")
    @Max(value = 10, message = "La puntuación máxima es 10")
    private Integer competencias;

    @Min(value = 0, message = "La puntuación mínima es 0")
    @Max(value = 10, message = "La puntuación máxima es 10")
    private Integer integracion;

    @Min(value = 0, message = "La puntuación mínima es 0")
    @Max(value = 10, message = "La puntuación máxima es 10")
    private Integer iniciativa;

    private String observaciones;

    private ResultadoEnum resultado;
}

