package com.mentorcore.model;


import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Perfil extendido del usuario con rol ADMIN.
 * Acceso total al sistema: gestión de usuarios, empresas,
 * cursos, periodos y configuración global.
 * RF10, RF11, RF12, RF13, RF14, RF20, RF21
 */
@Entity
@Table(name = "administrador")
@PrimaryKeyJoinColumn(name = "id_usuario")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class Administrador extends Usuario {

    // CONSTRUCTOR 
    
    public Administrador(String nombreUsuario, String email, String passwordHash,
                         String nombre, String apellidos) {
        super(nombreUsuario, email, passwordHash, nombre, apellidos,
                com.mentorcore.model.enums.RolEnum.ADMIN);
    }
}
