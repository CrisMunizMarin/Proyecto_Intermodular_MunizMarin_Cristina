package com.mentorcore.controller;

import com.mentorcore.model.Alumno;
import com.mentorcore.model.Asignacion;
import com.mentorcore.model.TutorEmpresa;
import com.mentorcore.service.AsignacionService;
import com.mentorcore.service.ConvenioService;
import com.mentorcore.service.DocumentoService;
import com.mentorcore.service.FaltaAsistenciaService;
import com.mentorcore.service.NotificacionService;
import com.mentorcore.service.TareaService;
import com.mentorcore.service.TipoDocumentoService;
import com.mentorcore.service.TutorEmpresaService;
import com.mentorcore.service.ValoracionService;
import com.mentorcore.util.FileUploadUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TutorEmpresaController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.mentorcore.exception.GlobalExceptionHandler.class)
public class TutorEmpresaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private TutorEmpresaService tutorEmpresaService;
    @MockBean private AsignacionService asignacionService;
    @MockBean private TareaService tareaService;
    @MockBean private FaltaAsistenciaService faltaAsistenciaService;
    @MockBean private DocumentoService documentoService;
    @MockBean private ConvenioService convenioService;
    @MockBean private NotificacionService notificacionService;
    @MockBean private ValoracionService valoracionService;
    @MockBean private TipoDocumentoService tipoDocumentoService;
    @MockBean private FileUploadUtil fileUploadUtil;

    @Test
    void crearConvenioInicial_redirigeConExitoParaAlumnoAsignado() throws Exception {
        TutorEmpresa tutor = new TutorEmpresa();
        tutor.setId(30L);
        tutor.setNombreUsuario("tutorempresa1");

        Alumno alumno = new Alumno();
        alumno.setId(5L);

        Asignacion asignacion = new Asignacion();
        asignacion.setAlumno(alumno);

        when(tutorEmpresaService.findByNombreUsuario("tutorempresa1")).thenReturn(Optional.of(tutor));
        when(asignacionService.findActivasByTutorEmpresaId(30L)).thenReturn(List.of(asignacion));
        when(asignacionService.findAsignacionActiva(alumno)).thenReturn(Optional.of(asignacion));

        mockMvc.perform(post("/tutor-empresa/documentos/convenio/crear")
                        .principal(() -> "tutorempresa1")
                        .param("idAlumno", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tutor-empresa/documentos"))
                .andExpect(flash().attribute("successMsg",
                        "Convenio inicial generado correctamente. Ya puedes subir el PDF."));

        verify(asignacionService).asegurarConvenioInicial(asignacion);
    }
}
