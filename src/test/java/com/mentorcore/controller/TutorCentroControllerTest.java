package com.mentorcore.controller;

import com.mentorcore.model.Alumno;
import com.mentorcore.model.Asignacion;
import com.mentorcore.model.TutorCentro;
import com.mentorcore.service.AlumnoService;
import com.mentorcore.service.AsignacionService;
import com.mentorcore.service.ConvenioService;
import com.mentorcore.service.DocumentoService;
import com.mentorcore.service.EmpresaService;
import com.mentorcore.service.FaltaAsistenciaService;
import com.mentorcore.service.InformeService;
import com.mentorcore.service.NotificacionService;
import com.mentorcore.service.TareaService;
import com.mentorcore.service.TipoDocumentoService;
import com.mentorcore.service.TutorCentroService;
import com.mentorcore.service.ValoracionService;
import com.mentorcore.util.FileUploadUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TutorCentroController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.mentorcore.exception.GlobalExceptionHandler.class)
public class TutorCentroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private AlumnoService alumnoService;
    @MockBean private TareaService tareaService;
    @MockBean private DocumentoService documentoService;
    @MockBean private ConvenioService convenioService;
    @MockBean private NotificacionService notificacionService;
    @MockBean private TutorCentroService tutorCentroService;
    @MockBean private ValoracionService valoracionService;
    @MockBean private InformeService informeService;
    @MockBean private EmpresaService empresaService;
    @MockBean private AsignacionService asignacionService;
    @MockBean private FaltaAsistenciaService faltaAsistenciaService;
    @MockBean private TipoDocumentoService tipoDocumentoService;
    @MockBean private FileUploadUtil fileUploadUtil;

    @Test
    void crearConvenioInicial_redirigeConExitoParaAlumnoAsignado() throws Exception {
        TutorCentro tutor = new TutorCentro();
        tutor.setId(10L);
        tutor.setNombreUsuario("tutorcentro1");

        Alumno alumno = new Alumno();
        alumno.setId(4L);
        alumno.setTutorCentro(tutor);

        Asignacion asignacion = new Asignacion();
        asignacion.setAlumno(alumno);

        when(tutorCentroService.findByNombreUsuario("tutorcentro1")).thenReturn(Optional.of(tutor));
        when(alumnoService.findById(4L)).thenReturn(Optional.of(alumno));
        when(asignacionService.findAsignacionActiva(alumno)).thenReturn(Optional.of(asignacion));

        mockMvc.perform(post("/tutor-centro/documentos/convenio/crear")
                        .principal(() -> "tutorcentro1")
                        .param("idAlumno", "4"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tutor-centro/documentos"))
                .andExpect(flash().attribute("successMsg",
                        "Convenio inicial generado correctamente. Ya puedes subir el PDF."));

        verify(asignacionService).asegurarConvenioInicial(asignacion);
    }
}
