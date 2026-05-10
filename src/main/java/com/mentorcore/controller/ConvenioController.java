package com.mentorcore.controller;

import com.mentorcore.model.Alumno;
import com.mentorcore.model.Convenio;
import com.mentorcore.model.Usuario;
import com.mentorcore.model.enums.RolEnum;
import com.mentorcore.service.AsignacionService;
import com.mentorcore.service.ConvenioService;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;

/**
 * Visualización/descarga segura de PDFs de convenio.
 */
@Controller
@RequestMapping("/convenios")
@RequiredArgsConstructor
@Slf4j
public class ConvenioController {

    private final ConvenioService convenioService;
    private final UsuarioService usuarioService;
    private final AsignacionService asignacionService;

    @GetMapping("/{idConvenio}/ver")
    public ResponseEntity<byte[]> verConvenio(@PathVariable Long idConvenio,
                                              Principal principal) throws IOException {
        Usuario usuario = getUsuarioAutenticado(principal);
        Convenio convenio = convenioService.findById(idConvenio)
                .orElseThrow(() -> new RuntimeException(
                        "Convenio no encontrado con id: " + idConvenio));

        validarAccesoConvenio(usuario, convenio);

        if (convenio.getArchivoPdfUrl() == null || convenio.getArchivoPdfUrl().isBlank()) {
            throw new RuntimeException("El convenio seleccionado no tiene un PDF asociado");
        }

        Path ruta = Path.of(convenio.getArchivoPdfUrl()).normalize();
        if (!Files.exists(ruta) || !Files.isReadable(ruta)) {
            throw new RuntimeException("El archivo PDF del convenio no está disponible");
        }

        byte[] contenido = Files.readAllBytes(ruta);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.inline()
                .filename("convenio-" + convenio.getNumeroConvenio() + ".pdf")
                .build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(contenido);
    }

    private Usuario getUsuarioAutenticado(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("No hay usuario autenticado");
        }

        return usuarioService.findByNombreUsuario(principal.getName())
                .orElseThrow(() -> new RuntimeException(
                        "Usuario no encontrado: " + principal.getName()));
    }

    private void validarAccesoConvenio(Usuario usuario, Convenio convenio) {
        Alumno alumno = convenio.getAlumno();

        if (usuario.getRol() == RolEnum.ADMIN) {
            return;
        }

        if (usuario.getRol() == RolEnum.ALUMNO && usuario.getId().equals(alumno.getId())) {
            return;
        }

        if (usuario.getRol() == RolEnum.TUTOR_CENTRO
                && alumno.getTutorCentro() != null
                && alumno.getTutorCentro().getId().equals(usuario.getId())) {
            return;
        }

        if (usuario.getRol() == RolEnum.TUTOR_EMPRESA
                && asignacionService.findAsignacionActiva(alumno)
                .map(asignacion -> asignacion.getTutorEmpresa().getId().equals(usuario.getId()))
                .orElse(false)) {
            return;
        }

        throw new RuntimeException("No tienes permisos para acceder a este convenio");
    }
}
