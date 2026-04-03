package com.mentorcore.model;

import com.mentorcore.model.enums.EstadoUsuarioEnum;
import com.mentorcore.model.enums.RolEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Entidad base de autenticación para todos los actores del sistema.
 * Todos los roles (Alumno, TutorCentro, TutorEmpresa, Admin) heredan de esta clase.
 * RF1, RF10, RF11 - RNF3, RNF4
 */
@Entity
@Table(name = "usuario")
@Inheritance(strategy = InheritanceType.JOINED)  // Cada subclase tiene su propia tabla
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "passwordHash")  // Nunca imprimir la contraseña por seguridad
public class Usuario {

    
    // IDENTIFICADOR

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    // CREDENCIALES DE ACCESO (RF1)

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
    @Column(name = "nombre_usuario", nullable = false, unique = true, length = 50)
    private String nombreUsuario;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;  // Almacena el hash BCrypt, nunca la contraseña en texto plano

   
    // DATOS PERSONALES (RF10, RF11)

    @NotBlank(message = "El nombre es obligatorio")
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Column(name = "apellidos", nullable = false, length = 150)
    private String apellidos;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "foto_perfil_url", length = 500)
    private String fotoPerfilUrl;

   
    // ROL Y ESTADO (RNF4 - Control de acceso RBAC)

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    private RolEnum rol;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoUsuarioEnum estado = EstadoUsuarioEnum.ACTIVO;  // Por defecto activo al crear

  
    // AUDITORÍA Y RECUPERACIÓN DE CONTRASEÑA (RF1)

    @Column(name = "ultimo_login")
    private LocalDateTime ultimoLogin;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "token_recuperacion", length = 255)
    private String tokenRecuperacion;  // UUID generado al solicitar recuperación

    @Column(name = "token_expira")
    private LocalDateTime tokenExpira;  // El token caduca tras 24h


    // CICLO DE VIDA JPA
    /**
     * Se ejecuta automáticamente antes de persistir el usuario por primera vez.
     * Establece la fecha de creación.
     */
    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = EstadoUsuarioEnum.ACTIVO;
        }
    }

 
    // CONSTRUCTOR 

    public Usuario(String nombreUsuario, String email, String passwordHash,
                   String nombre, String apellidos, RolEnum rol) {
        this.nombreUsuario = nombreUsuario;
        this.email = email;
        this.passwordHash = passwordHash;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.rol = rol;
        this.estado = EstadoUsuarioEnum.ACTIVO;
    }


    // MÉTODOS DE NEGOCIO
    /**
     * Devuelve el nombre completo del usuario.
     */
    public String getNombreCompleto() {
        return this.nombre + " " + this.apellidos;
    }

    /**
     * Comprueba si el usuario está activo y puede acceder al sistema.
     */
    public boolean isActivo() {
        return EstadoUsuarioEnum.ACTIVO.equals(this.estado);
    }

    /**
     * Comprueba si el token de recuperación sigue siendo válido.
     */
    public boolean isTokenValido() {
        return this.tokenRecuperacion != null
                && this.tokenExpira != null
                && LocalDateTime.now().isBefore(this.tokenExpira);
    }

    /**
     * Invalida el token de recuperación de contraseña tras su uso.
     */
    public void invalidarToken() {
        this.tokenRecuperacion = null;
        this.tokenExpira = null;
    }

    /**
     * Registra el momento del último acceso al sistema.
     */
    public void registrarLogin() {
        this.ultimoLogin = LocalDateTime.now();
    }
}
