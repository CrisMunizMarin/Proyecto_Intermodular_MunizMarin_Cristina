package com.mentorcore.controller;

import com.mentorcore.model.Alumno;
import com.mentorcore.model.CursoAcademico;
import com.mentorcore.model.Empresa;
import com.mentorcore.model.PeriodoFormacion;
import com.mentorcore.model.TutorCentro;
import com.mentorcore.model.Usuario;
import com.mentorcore.model.enums.RolEnum;
import com.mentorcore.service.AlumnoService;
import com.mentorcore.service.AsignacionService;
import com.mentorcore.service.CursoAcademicoService;
import com.mentorcore.service.EmpresaService;
import com.mentorcore.service.ParametroSistemaService;
import com.mentorcore.service.PeriodoFormacionService;
import com.mentorcore.service.TutorCentroService;
import com.mentorcore.service.TutorEmpresaService;
import com.mentorcore.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.mentorcore.exception.GlobalExceptionHandler.class)
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;
    @MockBean
    private EmpresaService empresaService;
    @MockBean
    private CursoAcademicoService cursoAcademicoService;
    @MockBean
    private ParametroSistemaService parametroSistemaService;
    @MockBean
    private AlumnoService alumnoService;
    @MockBean
    private TutorCentroService tutorCentroService;
    @MockBean
    private TutorEmpresaService tutorEmpresaService;
    @MockBean
    private AsignacionService asignacionService;
    @MockBean
    private PeriodoFormacionService periodoFormacionService;
    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    void crearUsuarioAlumno_guardaAlumnoConPasswordCifrada() throws Exception {
        CursoAcademico curso = new CursoAcademico();
        curso.setId(10L);
        TutorCentro tutorCentro = new TutorCentro();
        tutorCentro.setId(20L);

        when(cursoAcademicoService.findById(10L)).thenReturn(Optional.of(curso));
        when(tutorCentroService.findById(20L)).thenReturn(Optional.of(tutorCentro));
        when(passwordEncoder.encode("Password123")).thenReturn("$2a$hash");
        when(usuarioService.actualizar(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/admin/usuarios/crear")
                        .param("rol", RolEnum.ALUMNO.name())
                        .param("nombreUsuario", "alumnoNuevo")
                        .param("email", "alumno.nuevo@mentorcore.es")
                        .param("password", "Password123")
                        .param("nombre", "Lucia")
                        .param("apellidos", "Diaz")
                        .param("telefono", "600000111")
                        .param("idCurso", "10")
                        .param("idTutorCentro", "20")
                        .param("grupo", "2DAW-A")
                        .param("dni", "12345678A")
                        .param("fechaNacimiento", "2004-02-01")
                        .param("numSeguridadSocial", "28/1234567899"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/usuarios"))
                .andExpect(flash().attribute("successMsg", "Usuario creado correctamente."));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioService).actualizar(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(Alumno.class);

        Alumno guardado = (Alumno) captor.getValue();
        assertThat(guardado.getNombreUsuario()).isEqualTo("alumnoNuevo");
        assertThat(guardado.getPasswordHash()).isEqualTo("$2a$hash");
        assertThat(guardado.getCursoAcademico()).isSameAs(curso);
        assertThat(guardado.getTutorCentro()).isSameAs(tutorCentro);
        assertThat(guardado.getDni()).isEqualTo("12345678A");
        assertThat(guardado.getNumSeguridadSocial()).isEqualTo("28/1234567899");
    }

    @Test
    void crearUsuarioSinPassword_vuelveAlFormularioConError() throws Exception {
        when(cursoAcademicoService.findActivos()).thenReturn(List.of());
        when(tutorCentroService.findActivos()).thenReturn(List.of());
        when(empresaService.findActivas()).thenReturn(List.of());

        mockMvc.perform(post("/admin/usuarios/crear")
                        .param("rol", RolEnum.ADMIN.name())
                        .param("nombreUsuario", "admin2")
                        .param("email", "admin2@mentorcore.es")
                        .param("password", "")
                        .param("nombre", "Admin")
                        .param("apellidos", "Dos"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/usuarios/crear"))
                .andExpect(flash().attributeExists("errorMsg"));
    }
}
