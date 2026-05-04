package com.mentorcore.controller;

import com.mentorcore.model.Alumno;
import com.mentorcore.model.Documento;
import com.mentorcore.model.Usuario;
import com.mentorcore.model.enums.RolEnum;
import com.mentorcore.service.AsignacionService;
import com.mentorcore.service.DocumentoService;
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
 * Visualización/descarga segura de documentos del expediente.
 */
@Controller
@RequestMapping("/documentos")
@RequiredArgsConstructor
@Slf4j
public class DocumentoController {

    private final DocumentoService documentoService;
    private final UsuarioService usuarioService;
    private final AsignacionService asignacionService;

    @GetMapping("/{idDocumento}/ver")
    public ResponseEntity<byte[]> verDocumento(@PathVariable Long idDocumento,
                                               Principal principal) throws IOException {
        Usuario usuario = getUsuarioAutenticado(principal);
        Documento documento = documentoService.findById(idDocumento)
                .orElseThrow(() -> new RuntimeException(
                        "Documento no encontrado con id: " + idDocumento));

        validarAccesoDocumento(usuario, documento);

        Path ruta = Path.of(documento.getRutaAlmacenamiento()).normalize();
        if (!Files.exists(ruta) || !Files.isReadable(ruta)) {
            throw new RuntimeException("El archivo solicitado no está disponible");
        }

        byte[] contenido = Files.readAllBytes(ruta);
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (documento.getMimeType() != null && !documento.getMimeType().isBlank()) {
            mediaType = MediaType.parseMediaType(documento.getMimeType());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDisposition(ContentDisposition.inline()
                .filename(documento.getNombreArchivo())
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

    private void validarAccesoDocumento(Usuario usuario, Documento documento) {
        Alumno alumno = documento.getAlumno();

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

        throw new RuntimeException("No tienes permisos para acceder a este documento");
    }
}
