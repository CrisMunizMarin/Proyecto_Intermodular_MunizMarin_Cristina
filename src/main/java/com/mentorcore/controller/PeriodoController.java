package com.mentorcore.controller;

import com.mentorcore.model.CursoAcademico;
import com.mentorcore.model.PeriodoFormacion;
import com.mentorcore.model.Usuario;
import com.mentorcore.model.enums.RolEnum;
import com.mentorcore.model.enums.TipoPeriodoEnum;
import com.mentorcore.service.CursoAcademicoService;
import com.mentorcore.service.PeriodoFormacionService;
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

/**
 * Controlador de periodos de formación.
 * RF20
 */
@Controller
@RequestMapping("/periodos")
@RequiredArgsConstructor
@Slf4j
public class PeriodoController {

    private final PeriodoFormacionService periodoFormacionService;
    private final CursoAcademicoService cursoAcademicoService;
    private final UsuarioService usuarioService;

    @GetMapping
    public String listar(Model model, Principal principal) {
        validarAdmin(principal);

        model.addAttribute("periodos", periodoFormacionService.findAll());
        model.addAttribute("cursos", cursoAcademicoService.findAll());
        model.addAttribute("tiposPeriodo", TipoPeriodoEnum.values());

        return "admin/periodos";
    }

    @PostMapping("/crear")
    public String crear(@RequestParam("idCurso") Long idCurso,
                        @RequestParam("tipo") TipoPeriodoEnum tipo,
                        @RequestParam("anioAcademico") String anioAcademico,
                        @RequestParam("fechaInicio") String fechaInicio,
                        @RequestParam("fechaFin") String fechaFin,
                        @RequestParam(value = "horasTotales", required = false) Integer horasTotales,
                        @RequestParam(value = "descripcion", required = false) String descripcion,
                        Principal principal,
                        RedirectAttributes redirectAttributes) {
        try {
            Usuario admin = validarAdmin(principal);

            CursoAcademico curso = cursoAcademicoService.findById(idCurso)
                    .orElseThrow(() -> new RuntimeException(
                            "Curso no encontrado con id: " + idCurso));

            PeriodoFormacion periodo = new PeriodoFormacion();
            periodo.setCursoAcademico(curso);
            periodo.setTipo(tipo);
            periodo.setAnioAcademico(anioAcademico);
            periodo.setFechaInicio(LocalDate.parse(fechaInicio));
            periodo.setFechaFin(LocalDate.parse(fechaFin));
            periodo.setHorasTotales(horasTotales != null ? horasTotales : 400);
            periodo.setDescripcion(descripcion);
            periodo.setCreadoPor(admin);

            periodoFormacionService.crear(periodo);

            redirectAttributes.addFlashAttribute("successMsg",
                    "Periodo creado correctamente.");
        } catch (Exception e) {
            ControllerMessageUtil.addSafeErrorMessage(
                    redirectAttributes,
                    log,
                    "Error al crear periodo de formacion",
                    e,
                    "No se pudo crear el periodo. Inténtalo de nuevo."
            );
        }

        return "redirect:/periodos";
    }

    @PostMapping("/{id}/activar")
    public String activar(@PathVariable Long id,
                          Principal principal,
                          RedirectAttributes redirectAttributes) {
        try {
            validarAdmin(principal);
            periodoFormacionService.activar(id);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Periodo activado correctamente.");
        } catch (Exception e) {
            ControllerMessageUtil.addSafeErrorMessage(
                    redirectAttributes,
                    log,
                    "Error al activar periodo de formacion",
                    e,
                    "No se pudo activar el periodo. Inténtalo de nuevo."
            );
        }

        return "redirect:/periodos";
    }

    @PostMapping("/{id}/cerrar")
    public String cerrar(@PathVariable Long id,
                         Principal principal,
                         RedirectAttributes redirectAttributes) {
        try {
            validarAdmin(principal);
            periodoFormacionService.cerrar(id);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Periodo cerrado correctamente.");
        } catch (Exception e) {
            ControllerMessageUtil.addSafeErrorMessage(
                    redirectAttributes,
                    log,
                    "Error al cerrar periodo de formacion",
                    e,
                    "No se pudo cerrar el periodo. Inténtalo de nuevo."
            );
        }

        return "redirect:/periodos";
    }

    private Usuario validarAdmin(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("No hay usuario autenticado");
        }

        Usuario usuario = usuarioService.findByNombreUsuario(principal.getName())
                .orElseThrow(() -> new RuntimeException(
                        "Usuario no encontrado: " + principal.getName()));

        if (usuario.getRol() != RolEnum.ADMIN) {
            throw new RuntimeException("Acceso denegado: solo el administrador puede gestionar periodos");
        }

        return usuario;
    }
}
