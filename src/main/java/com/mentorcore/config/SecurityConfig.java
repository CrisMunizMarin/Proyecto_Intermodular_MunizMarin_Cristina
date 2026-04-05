package com.mentorcore.config;

import com.mentorcore.service.UsuarioService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * Configuración central de Spring Security.
 * Define rutas protegidas, autenticación por roles y BCrypt.
 * RNF3, RNF4 - RF1
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Permite usar @PreAuthorize en controladores
public class SecurityConfig {

    // BEANS DE SEGURIDAD

    /**
     * Codificador de contraseñas BCrypt con coste 12.
     * NUNCA almacenar contraseñas en texto plano. RNF3
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Proveedor de autenticación que usa UsuarioService + BCrypt.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider(UsuarioService usuarioService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(usuarioService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Gestor de autenticación necesario para el login manual.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Redirige a cada rol a su panel correspondiente tras el login. RF1
     */
    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return (request, response, authentication) -> {
            String rol = authentication.getAuthorities()
                    .iterator().next().getAuthority();
            switch (rol) {
                case "ROLE_ALUMNO"         -> response.sendRedirect("/alumno/inicio");
                case "ROLE_TUTOR_CENTRO"   -> response.sendRedirect("/tutor-centro/inicio");
                case "ROLE_TUTOR_EMPRESA"  -> response.sendRedirect("/tutor-empresa/inicio");
                case "ROLE_ADMIN"          -> response.sendRedirect("/admin/inicio");
                default                    -> response.sendRedirect("/");
            }
        };
    }

    // CADENA DE FILTROS DE SEGURIDAD

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            UsuarioService usuarioService) throws Exception {
        http
           
            // AUTORIZACIÓN DE RUTAS
           
            .authorizeHttpRequests(auth -> auth

                // Rutas públicas: accesibles sin autenticación
                .requestMatchers(
                    "/",
                    "/auth/login",
                    "/auth/recuperar-password",
                    "/auth/reset-password",
                    "/css/**",
                    "/js/**",
                    "/img/**",
                    "/actuator/health"
                ).permitAll()

                // Rutas del alumno
                .requestMatchers("/alumno/**")
                    .hasRole("ALUMNO")

                // Rutas del tutor centro
                .requestMatchers("/tutor-centro/**")
                    .hasRole("TUTOR_CENTRO")

                // Rutas del tutor empresa
                .requestMatchers("/tutor-empresa/**")
                    .hasRole("TUTOR_EMPRESA")

                // Rutas del administrador
                .requestMatchers("/admin/**")
                    .hasRole("ADMIN")

                // Cualquier otra ruta requiere autenticación
                .anyRequest().authenticated()
            )

          
            // FORMULARIO DE LOGIN
            
            .formLogin(form -> form
                .loginPage("/auth/login")           // Vista personalizada de login
                .loginProcessingUrl("/auth/login")  // URL que procesa el formulario POST
                .usernameParameter("nombreUsuario") // Nombre del campo en el HTML
                .passwordParameter("password")
                .successHandler(successHandler())   // Redirige según el rol
                .failureUrl("/auth/login?error=true") // Redirige si falla
                .permitAll()
            )

           
            // LOGOUT
            
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/auth/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )

            
            // PROVEEDOR DE AUTENTICACIÓN
           
            .authenticationProvider(authenticationProvider(usuarioService));

        return http.build();
    }
}
