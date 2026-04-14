package com.mentorcore.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO para creación y edición de empresas colaboradoras.
 * RF18
 */
@Data
public class EmpresaDTO {

    private Long id;

    @NotBlank(message = "El nombre de la empresa es obligatorio")
    private String nombre;

    @NotBlank(message = "El CIF es obligatorio")
    private String cif;

    private String sector;
    private String direccion;
    private String municipio;
    private String provincia;
    private String codigoPostal;
    private String telefono;

    @Email(message = "El formato del email no es válido")
    private String emailContacto;

    private String web;
    private String notas;
}

