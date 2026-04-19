package com.mentorcore.controller;

import com.mentorcore.model.Alumno;
import com.mentorcore.model.Asignacion;
import com.mentorcore.model.Empresa;
import com.mentorcore.model.PeriodoFormacion;
import com.mentorcore.model.TutorCentro;
import com.mentorcore.model.TutorEmpresa;
import com.mentorcore.model.Usuario;
import com.mentorcore.model.enums.RolEnum;
import com.mentorcore.service.AlumnoService;
import com.mentorcore.service.AsignacionService;
import com.mentorcore.service.EmpresaService;
import com.mentorcore.service.NotificacionService;
import com.mentorcore.service.PeriodoFormacionService;
import com.mentorcore.service.TutorEmpresaService;
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
 * Controlador genérico de asignaciones.
 * RF13, RF21
 */
@Controller
@RequestMapping("/asignaciones")
@RequiredArgsConstructor
@Slf4j
public class AsignacionController {

    private final UsuarioService usuarioService;
    private final AlumnoService alumnoService;
    private final EmpresaService empresaService;
    private final TutorEmpresaService tutorEmpresaService;
    private final PeriodoFormacionService periodoFormacionService;
    private final AsignacionService asignacionService;
    private final NotificacionService notificacionService;

    @PostMapping("/reasignar/{idAlumno}")
    public String reasignar(@PathVariable Long idAlumno,
                            @RequestParam("idEmpresa") Long idEmpresa,
                            @RequestParam("idTutorEmpresa") Long idTutorEmpresa,
                            @RequestParam("idPeriodo") Long idPeriodo,
                            @RequestParam("nuevaFechaInicio") String nuevaFechaInicio,
                            @RequestParam(value = "motivoCambio", required = false) String motivoCambio,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = getUsuarioAutenticado(principal);

            Alumno alumno = alumnoService.findById(idAlumno)
                    .orElseThrow(() -> new RuntimeException(
                            "Alumno no encontrado con id: " + idAlumno));

            validarGestionAlumno(usuario, alumno);

            Empresa empresa = empresaService.findById(idEmpresa)
                    .orElseThrow(() -> new RuntimeException(
                            "Empresa no encontrada con id: " + idEmpresa));

            TutorEmpresa tutorEmpresa = tutorEmpresaService.findById(idTutorEmpresa)
                    .orElseThrow(() -> new RuntimeException(
                            "Tutor de empresa no encontrado con id: " + idTutorEmpresa));

            PeriodoFormacion periodo = periodoFormacionService.findById(idPeriodo)
                    .orElseThrow(() -> new RuntimeException(
                            "Periodo no encontrado con id: " + idPeriodo));

            asignacionService.reasignar(
                    alumno,
                    empresa,
                    tutorEmpresa,
                    periodo,
                    LocalDate.parse(nuevaFechaInicio),
                    motivoCambio,
                    usuario
            );

            notificacionService.notificarReasignacion(
                    alumno,
                    tutorEmpresa,
                    empresa.getNombre()
            );

            redirectAttributes.addFlashAttribute("successMsg",
                    "Alumno reasignado correctamente.");
        } catch (Exception e) {
            ControllerMessageUtil.addSafeErrorMessage(
                    redirectAttributes,
                    log,
                    "Error al reasignar alumno",
                    e,
                    "No se pudo completar la reasignación. Inténtalo de nuevo."
            );
        }

        return "redirect:/admin/asignaciones";
    }

    @PostMapping("/{idAsignacion}/finalizar")
    public String finalizar(@PathVariable Long idAsignacion,
                            @RequestParam("fechaFin") String fechaFin,
                            @RequestParam(value = "motivo", required = false) String motivo,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = getUsuarioAutenticado(principal);

            Asignacion asignacion = asignacionService.findById(idAsignacion)
                    .orElseThrow(() -> new RuntimeException(
                            "Asignación no encontrada con id: " + idAsignacion));

            validarGestionAlumno(usuario, asignacion.getAlumno());

            asignacionService.finalizar(
                    asignacion,
                    LocalDate.parse(fechaFin),
                    motivo
            );

            redirectAttributes.addFlashAttribute("successMsg",
                    "Asignación finalizada correctamente.");
        } catch (Exception e) {
            ControllerMessageUtil.addSafeErrorMessage(
                    redirectAttributes,
                    log,
                    "Error al finalizar asignacion",
                    e,
                    "No se pudo finalizar la asignación. Inténtalo de nuevo."
            );
        }

        return "redirect:/admin/asignaciones";
    }

    private Usuario getUsuarioAutenticado(Principal principal) {
        return usuarioService.findByNombreUsuario(principal.getName())
                .orElseThrow(() -> new RuntimeException(
                        "Usuario no autenticado"));
    }

    private void validarGestionAlumno(Usuario usuario, Alumno alumno) {
        if (usuario.getRol() == RolEnum.ADMIN) {
            return;
        }

        if (usuario instanceof TutorCentro tutorCentro &&
                alumno.getTutorCentro() != null &&
                alumno.getTutorCentro().getId().equals(tutorCentro.getId())) {
            return;
        }

        throw new RuntimeException("No tienes permisos para gestionar este alumno");
    }
}
