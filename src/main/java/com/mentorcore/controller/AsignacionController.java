package com.mentorcore.controller;

import com.mentorcore.model.Alumno;
import com.mentorcore.model.Asignacion;
import com.mentorcore.model.Empresa;
import com.mentorcore.model.PeriodoFormacion;
import com.mentorcore.model.TutorEmpresa;
import com.mentorcore.model.Usuario;
import com.mentorcore.model.enums.EstadoFeEnum;
import com.mentorcore.model.enums.EstadoPeriodoEnum;
import com.mentorcore.model.enums.RolEnum;
import com.mentorcore.model.enums.TipoNotificacionEnum;
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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.Set;

/**
 * Controlador de asignaciones del administrador.
 * RF13, RF21
 */
@Controller
@RequestMapping("/admin/asignaciones")
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

    @GetMapping
    public String listar(Model model, Principal principal) {
        Usuario admin = validarAdmin(principal);

        List<Asignacion> asignaciones = asignacionService.findAll();

        model.addAttribute("adminActual", admin);
        model.addAttribute("asignacionesActivas",
                asignaciones.stream()
                        .filter(asignacion -> asignacion.getEstado() == EstadoFeEnum.EN_CURSO)
                        .toList());
        model.addAttribute("asignacionesHistoricas", asignaciones);
        List<Alumno> alumnos = alumnoService.findAll();
        Set<Long> alumnosConAsignacionActivaIds = alumnos.stream()
                .filter(asignacionService::tieneAsignacionActiva)
                .map(Alumno::getId)
                .collect(java.util.stream.Collectors.toSet());

        model.addAttribute("alumnos", alumnos);
        model.addAttribute("alumnosConAsignacionActivaIds", alumnosConAsignacionActivaIds);
        model.addAttribute("empresasActivas", empresaService.findActivas());
        model.addAttribute("tutoresEmpresa", tutorEmpresaService.findAll());
        model.addAttribute("periodosDisponibles",
                periodoFormacionService.findAll().stream()
                        .filter(periodo -> periodo.getEstado() != EstadoPeriodoEnum.CERRADO)
                        .toList());

        return "admin/asignaciones";
    }

    @PostMapping("/crear")
    public String crear(@RequestParam("idAlumno") Long idAlumno,
                        @RequestParam("idEmpresa") Long idEmpresa,
                        @RequestParam("idTutorEmpresa") Long idTutorEmpresa,
                        @RequestParam("idPeriodo") Long idPeriodo,
                        @RequestParam("fechaInicio") String fechaInicio,
                        Principal principal,
                        RedirectAttributes redirectAttributes) {
        try {
            validarAdmin(principal);

            Alumno alumno = alumnoService.findById(idAlumno)
                    .orElseThrow(() -> new RuntimeException(
                            "Alumno no encontrado con id: " + idAlumno));
            Empresa empresa = empresaService.findById(idEmpresa)
                    .orElseThrow(() -> new RuntimeException(
                            "Empresa no encontrada con id: " + idEmpresa));
            TutorEmpresa tutorEmpresa = tutorEmpresaService.findById(idTutorEmpresa)
                    .orElseThrow(() -> new RuntimeException(
                            "Tutor de empresa no encontrado con id: " + idTutorEmpresa));
            PeriodoFormacion periodo = periodoFormacionService.findById(idPeriodo)
                    .orElseThrow(() -> new RuntimeException(
                            "Periodo no encontrado con id: " + idPeriodo));

            validarTutorEmpresa(tutorEmpresa, empresa);

            asignacionService.crear(
                    alumno,
                    empresa,
                    tutorEmpresa,
                    periodo,
                    LocalDate.parse(fechaInicio)
            );

            notificacionService.enviarSistema(
                    alumno,
                    TipoNotificacionEnum.AVISO,
                    "Nueva asignación de prácticas",
                    "Se te ha asignado la empresa " + empresa.getNombre() + "."
            );
            notificacionService.enviarSistema(
                    tutorEmpresa,
                    TipoNotificacionEnum.AVISO,
                    "Nuevo alumno asignado",
                    "Se te ha asignado el alumno " + alumno.getNombreCompleto() + "."
            );

            redirectAttributes.addFlashAttribute("successMsg",
                    "Asignación creada correctamente.");
        } catch (Exception e) {
            ControllerMessageUtil.addSafeErrorMessage(
                    redirectAttributes,
                    log,
                    "Error al crear asignacion",
                    e,
                    "No se pudo crear la asignación. Inténtalo de nuevo."
            );
        }

        return "redirect:/admin/asignaciones";
    }

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
            Usuario usuario = validarAdmin(principal);

            Alumno alumno = alumnoService.findById(idAlumno)
                    .orElseThrow(() -> new RuntimeException(
                            "Alumno no encontrado con id: " + idAlumno));
            Empresa empresa = empresaService.findById(idEmpresa)
                    .orElseThrow(() -> new RuntimeException(
                            "Empresa no encontrada con id: " + idEmpresa));
            TutorEmpresa tutorEmpresa = tutorEmpresaService.findById(idTutorEmpresa)
                    .orElseThrow(() -> new RuntimeException(
                            "Tutor de empresa no encontrado con id: " + idTutorEmpresa));
            PeriodoFormacion periodo = periodoFormacionService.findById(idPeriodo)
                    .orElseThrow(() -> new RuntimeException(
                            "Periodo no encontrado con id: " + idPeriodo));

            validarTutorEmpresa(tutorEmpresa, empresa);

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
            validarAdmin(principal);

            Asignacion asignacion = asignacionService.findById(idAsignacion)
                    .orElseThrow(() -> new RuntimeException(
                            "Asignación no encontrada con id: " + idAsignacion));

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

    private Usuario validarAdmin(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("No hay usuario autenticado");
        }

        Usuario usuario = usuarioService.findByNombreUsuario(principal.getName())
                .orElseThrow(() -> new RuntimeException(
                        "Usuario no autenticado"));

        if (usuario.getRol() != RolEnum.ADMIN) {
            throw new RuntimeException("Acceso denegado: solo el administrador puede gestionar asignaciones");
        }

        return usuario;
    }

    private void validarTutorEmpresa(TutorEmpresa tutorEmpresa, Empresa empresa) {
        if (tutorEmpresa.getEmpresa() == null ||
                !tutorEmpresa.getEmpresa().getId().equals(empresa.getId())) {
            throw new RuntimeException(
                    "El tutor de empresa seleccionado no pertenece a la empresa elegida.");
        }
    }
}
