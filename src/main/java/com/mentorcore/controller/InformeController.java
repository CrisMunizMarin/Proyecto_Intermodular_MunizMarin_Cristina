package com.mentorcore.controller;

import com.mentorcore.model.Alumno;
import com.mentorcore.model.TutorCentro;
import com.mentorcore.model.Usuario;
import com.mentorcore.model.enums.RolEnum;
import com.mentorcore.service.AlumnoService;
import com.mentorcore.service.InformeService;
import com.mentorcore.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

/**
 * Controlador genérico de informes.
 * RF8, RF14
 */
@Controller
@RequestMapping("/informes")
@RequiredArgsConstructor
@Slf4j
public class InformeController {

    private final UsuarioService usuarioService;
    private final AlumnoService alumnoService;
    private final InformeService informeService;

    @GetMapping("/alumno/{idAlumno}/pdf")
    public ResponseEntity<byte[]> descargarInformeAlumno(@PathVariable Long idAlumno,
                                                         Principal principal) {
        Usuario usuario = getUsuarioAutenticado(principal);
        Alumno alumno = alumnoService.findById(idAlumno)
                .orElseThrow(() -> new RuntimeException(
                        "Alumno no encontrado con id: " + idAlumno));

        validarAccesoInforme(usuario, alumno);

        byte[] pdf = informeService.generarInformePdf(alumno);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("informe-" + alumno.getNombreUsuario() + ".pdf")
                .build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }

    private Usuario getUsuarioAutenticado(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("No hay usuario autenticado");
        }

        return usuarioService.findByNombreUsuario(principal.getName())
                .orElseThrow(() -> new RuntimeException(
                        "Usuario no encontrado: " + principal.getName()));
    }

    private void validarAccesoInforme(Usuario usuario, Alumno alumno) {
        if (usuario.getRol() == RolEnum.ADMIN) {
            return;
        }

        if (usuario.getRol() == RolEnum.ALUMNO && usuario.getId().equals(alumno.getId())) {
            return;
        }

        if (usuario instanceof TutorCentro tutorCentro &&
                alumno.getTutorCentro() != null &&
                alumno.getTutorCentro().getId().equals(tutorCentro.getId())) {
            return;
        }

        throw new RuntimeException("No tienes permisos para acceder a este informe");
    }
}

