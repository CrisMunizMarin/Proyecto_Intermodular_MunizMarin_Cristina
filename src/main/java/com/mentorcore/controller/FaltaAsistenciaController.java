package com.mentorcore.controller;

import com.mentorcore.model.Alumno;
import com.mentorcore.model.Asignacion;
import com.mentorcore.model.FaltaAsistencia;
import com.mentorcore.model.TutorCentro;
import com.mentorcore.model.TutorEmpresa;
import com.mentorcore.service.AsignacionService;
import com.mentorcore.service.FaltaAsistenciaService;
import com.mentorcore.service.NotificacionService;
import com.mentorcore.service.UsuarioService;
import com.mentorcore.util.ControllerMessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;

/**
 * Controlador genérico de faltas de asistencia.
 * RF19, RF22
 */
@Controller
@RequestMapping("/faltas")
@RequiredArgsConstructor
@Slf4j
public class FaltaAsistenciaController {

    private final UsuarioService usuarioService;
    private final AsignacionService asignacionService;
    private final FaltaAsistenciaService faltaAsistenciaService;
    private final NotificacionService notificacionService;

    @PostMapping("/registrar/{idAlumno}")
    public String registrar(@PathVariable Long idAlumno,
                            @RequestParam("fechaFalta") String fechaFalta,
                            @RequestParam("tipo") com.mentorcore.model.enums.TipoFaltaEnum tipo,
                            @RequestParam(value = "observacion", required = false) String observacion,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {
        try {
            TutorEmpresa tutor = getTutorEmpresaAutenticado(principal);
            Alumno alumno = getAlumnoAsignado(idAlumno, tutor);

            Asignacion asignacion = asignacionService.findAsignacionActiva(alumno)
                    .orElseThrow(() -> new RuntimeException(
                            "El alumno no tiene asignación activa"));

            FaltaAsistencia falta = faltaAsistenciaService.registrar(
                    alumno,
                    asignacion,
                    tutor,
                    LocalDate.parse(fechaFalta),
                    tipo,
                    observacion
            );

            if (alumno.getTutorCentro() != null) {
                notificacionService.notificarFaltaAsistencia(
                        alumno,
                        alumno.getTutorCentro(),
                        falta.getFechaFalta().toString(),
                        falta.getTipo().name()
                );
            }

            redirectAttributes.addFlashAttribute("successMsg",
                    "Falta registrada correctamente.");
        } catch (Exception e) {
            ControllerMessageUtil.addSafeErrorMessage(
                    redirectAttributes,
                    log,
                    "Error al registrar falta de asistencia",
                    e,
                    "No se pudo registrar la falta. Inténtalo de nuevo."
            );
        }

        return "redirect:/tutor-empresa/faltas";
    }

    @PostMapping("/{idFalta}/aprobar")
    public String aprobar(@PathVariable Long idFalta,
                          Principal principal,
                          RedirectAttributes redirectAttributes) {
        try {
            TutorCentro tutor = getTutorCentroAutenticado(principal);

            FaltaAsistencia falta = faltaAsistenciaService.findById(idFalta)
                    .orElseThrow(() -> new RuntimeException(
                            "Falta no encontrada con id: " + idFalta));

            Alumno alumno = falta.getAlumno();
            if (alumno.getTutorCentro() == null ||
                    !alumno.getTutorCentro().getId().equals(tutor.getId())) {
                throw new RuntimeException("No puedes revisar faltas de un alumno no asignado");
            }

            faltaAsistenciaService.aprobarJustificante(idFalta, tutor);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Justificante aprobado correctamente.");
        } catch (Exception e) {
            ControllerMessageUtil.addSafeErrorMessage(
                    redirectAttributes,
                    log,
                    "Error al aprobar justificante de falta",
                    e,
                    "No se pudo aprobar el justificante. Inténtalo de nuevo."
            );
        }

        return "redirect:/tutor-centro/documentos";
    }

    @PostMapping("/{idFalta}/denegar")
    public String denegar(@PathVariable Long idFalta,
                          @RequestParam(value = "motivo", required = false) String motivo,
                          Principal principal,
                          RedirectAttributes redirectAttributes) {
        try {
            TutorCentro tutor = getTutorCentroAutenticado(principal);

            FaltaAsistencia falta = faltaAsistenciaService.findById(idFalta)
                    .orElseThrow(() -> new RuntimeException(
                            "Falta no encontrada con id: " + idFalta));

            Alumno alumno = falta.getAlumno();
            if (alumno.getTutorCentro() == null ||
                    !alumno.getTutorCentro().getId().equals(tutor.getId())) {
                throw new RuntimeException("No puedes revisar faltas de un alumno no asignado");
            }

            faltaAsistenciaService.denegarJustificante(idFalta, tutor, motivo);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Justificante denegado correctamente.");
        } catch (Exception e) {
            ControllerMessageUtil.addSafeErrorMessage(
                    redirectAttributes,
                    log,
                    "Error al denegar justificante de falta",
                    e,
                    "No se pudo denegar el justificante. Inténtalo de nuevo."
            );
        }

        return "redirect:/tutor-centro/documentos";
    }

    private TutorEmpresa getTutorEmpresaAutenticado(Principal principal) {
        return usuarioService.findByNombreUsuario(principal.getName())
                .filter(usuario -> usuario instanceof TutorEmpresa)
                .map(usuario -> (TutorEmpresa) usuario)
                .orElseThrow(() -> new RuntimeException(
                        "Tutor de empresa no autenticado"));
    }

    private TutorCentro getTutorCentroAutenticado(Principal principal) {
        return usuarioService.findByNombreUsuario(principal.getName())
                .filter(usuario -> usuario instanceof TutorCentro)
                .map(usuario -> (TutorCentro) usuario)
                .orElseThrow(() -> new RuntimeException(
                        "Tutor de centro no autenticado"));
    }

    private Alumno getAlumnoAsignado(Long idAlumno, TutorEmpresa tutor) {
        return asignacionService.findActivasByTutorEmpresa(tutor).stream()
                .map(Asignacion::getAlumno)
                .filter(alumno -> alumno.getId().equals(idAlumno))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "El alumno no está asignado a este tutor de empresa"));
    }
}
