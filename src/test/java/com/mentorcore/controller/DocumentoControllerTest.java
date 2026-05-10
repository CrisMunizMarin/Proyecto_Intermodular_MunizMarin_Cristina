package com.mentorcore.controller;

import com.mentorcore.model.Alumno;
import com.mentorcore.model.Documento;
import com.mentorcore.model.TutorCentro;
import com.mentorcore.model.Usuario;
import com.mentorcore.model.enums.RolEnum;
import com.mentorcore.service.AsignacionService;
import com.mentorcore.service.DocumentoService;
import com.mentorcore.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentoController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.mentorcore.exception.GlobalExceptionHandler.class)
class DocumentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentoService documentoService;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private AsignacionService asignacionService;

    @TempDir
    Path tempDir;

    @Test
    void verDocumento_permiteAccesoAlAlumnoPropio() throws Exception {
        Usuario alumnoUsuario = new Usuario();
        alumnoUsuario.setId(7L);
        alumnoUsuario.setRol(RolEnum.ALUMNO);
        alumnoUsuario.setNombreUsuario("alumno1");

        Alumno alumno = new Alumno();
        alumno.setId(7L);

        Path archivo = tempDir.resolve("dni.pdf");
        Files.writeString(archivo, "contenido-prueba");

        Documento documento = new Documento();
        documento.setId(90L);
        documento.setAlumno(alumno);
        documento.setNombreArchivo("dni.pdf");
        documento.setRutaAlmacenamiento(archivo.toString());
        documento.setMimeType("application/pdf");

        when(usuarioService.findByNombreUsuario("alumno1")).thenReturn(Optional.of(alumnoUsuario));
        when(documentoService.findById(90L)).thenReturn(Optional.of(documento));

        mockMvc.perform(get("/documentos/90/ver").principal(() -> "alumno1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(content().bytes(Files.readAllBytes(archivo)));
    }

    @Test
    void verDocumento_deniegaAccesoASinPermiso() throws Exception {
        Usuario tutorCentroUsuario = new Usuario();
        tutorCentroUsuario.setId(11L);
        tutorCentroUsuario.setRol(RolEnum.TUTOR_CENTRO);
        tutorCentroUsuario.setNombreUsuario("tc2");

        TutorCentro tutorAsignado = new TutorCentro();
        tutorAsignado.setId(99L);

        Alumno alumno = new Alumno();
        alumno.setId(7L);
        alumno.setTutorCentro(tutorAsignado);

        Path archivo = tempDir.resolve("seguro.pdf");
        Files.writeString(archivo, "contenido");

        Documento documento = new Documento();
        documento.setId(91L);
        documento.setAlumno(alumno);
        documento.setNombreArchivo("seguro.pdf");
        documento.setRutaAlmacenamiento(archivo.toString());
        documento.setMimeType("application/pdf");

        when(usuarioService.findByNombreUsuario("tc2")).thenReturn(Optional.of(tutorCentroUsuario));
        when(documentoService.findById(91L)).thenReturn(Optional.of(documento));

        mockMvc.perform(get("/documentos/91/ver").principal(() -> "tc2"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Se ha producido un error")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "No se pudo completar la operación en este momento.")));
    }
}
