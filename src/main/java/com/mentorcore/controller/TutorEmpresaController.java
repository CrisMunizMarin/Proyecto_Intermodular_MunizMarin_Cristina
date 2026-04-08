package com.mentorcore.controller;

import com.mentorcore.model.Alumno;
import com.mentorcore.model.Asignacion;
import com.mentorcore.model.Notificacion;
import com.mentorcore.model.TutorEmpresa;
import com.mentorcore.model.Valoracion;
import com.mentorcore.model.FaltaAsistencia;
import com.mentorcore.model.Tarea;
import com.mentorcore.model.enums.ResultadoEnum;
import com.mentorcore.model.enums.TipoFaltaEnum;
import com.mentorcore.model.enums.TipoEvaluadorEnum;
import com.mentorcore.service.AsignacionService;
import com.mentorcore.service.DocumentoService;
import com.mentorcore.service.FaltaAsistenciaService;
import com.mentorcore.service.NotificacionService;
import com.mentorcore.service.TareaService;
import com.mentorcore.service.TutorEmpresaService;
import com.mentorcore.service.UsuarioService;
import com.mentorcore.service.ValoracionService;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador del panel del tutor de empresa.
 * Gestiona la supervisión diaria del alumno en la empresa:
 * tareas, faltas, documentos, notificaciones y valoración final.
 * RF6, RF7, RF9, RF19, RF22
 */
@Controller
@RequestMapping("/tutor-empresa")
@RequiredArgsConstructor
@Slf4j
public class TutorEmpresaController {

    private final UsuarioService usuarioService;
    private final TutorEmpresaService tutorEmpresaService;
    private final AsignacionService asignacionService;
    private final TareaService tareaService;
    private final FaltaAsistenciaService faltaAsistenciaService;
    private final DocumentoService documentoService;
    private final NotificacionService notificacionService;
    private final ValoracionService valoracionService;


    // INICIO

    @GetMapping("/inicio")
    public String inicio(Model model, Principal principal) {
        TutorEmpresa tutor = getTutorEmpresaAutenticado(principal);
        cargarDatosBase(model, tutor);

        List<Asignacion> asignacionesActivas = asignacionService.findActivasByTutorEmpresa(tutor);

        model.addAttribute("asignacionesActivas", asignacionesActivas);
        model.addAttribute("totalAlumnos", asignacionesActivas.size());
        model.addAttribute("notificacionesNoLeidas",
                notificacionService.contarNoLeidas(tutor));

        return "tutor-empresa/inicio";
    }


    // BÚSQUEDA / ALUMNOS ASIGNADOS

    @GetMapping("/busqueda-alumno")
    public String busquedaAlumno(Model model, Principal principal) {
        TutorEmpresa tutor = getTutorEmpresaAutenticado(principal);
        cargarDatosBase(model, tutor);

        List<Asignacion> asignaciones = asignacionService.findActivasByTutorEmpresa(tutor);
        List<Alumno> alumnos = new ArrayList<>();

        for (Asignacion asignacion : asignaciones) {
            alumnos.add(asignacion.getAlumno());
        }

        model.addAttribute("alumnos", alumnos);
        model.addAttribute("asignaciones", asignaciones);

        return "tutor-empresa/busqueda-alumno";
    }


    // TAREAS

    @GetMapping("/tareas")
    public String tareas(Model model, Principal principal) {
        TutorEmpresa tutor = getTutorEmpresaAutenticado(principal);
        cargarDatosBase(model, tutor);

        List<Asignacion> asignaciones = asignacionService.findActivasByTutorEmpresa(tutor);
        model.addAttribute("asignaciones", asignaciones);

        return "tutor-empresa/tareas";
    }


    // DOCUMENTOS

    @GetMapping("/documentos")
    public String documentos(Model model, Principal principal) {
        TutorEmpresa tutor = getTutorEmpresaAutenticado(principal);
        cargarDatosBase(model, tutor);

        List<Asignacion> asignaciones = asignacionService.findActivasByTutorEmpresa(tutor);
        model.addAttribute("asignaciones", asignaciones);

        return "tutor-empresa/documentos";
    }


    // FALTAS

    @GetMapping("/faltas")
    public String faltas(Model model, Principal principal) {
        TutorEmpresa tutor = getTutorEmpresaAutenticado(principal);
        cargarDatosBase(model, tutor);

        List<Asignacion> asignaciones = asignacionService.findActivasByTutorEmpresa(tutor);
        model.addAttribute("asignaciones", asignaciones);

        return "tutor-empresa/faltas";
    }


    // NOTIFICACIONES

    @GetMapping("/notificaciones")
    public String notificaciones(Model model, Principal principal) {
        TutorEmpresa tutor = getTutorEmpresaAutenticado(principal);
        cargarDatosBase(model, tutor);

        List<Notificacion> notificaciones = notificacionService.findByReceptor(tutor);
        model.addAttribute("notificaciones", notificaciones);
        model.addAttribute("totalNoLeidas",
                notificacionService.contarNoLeidas(tutor));

        return "tutor-empresa/notificaciones";
    }

    @PostMapping("/notificaciones/{id}/leer")
    public String marcarNotificacionLeida(@PathVariable Long id,
                                          RedirectAttributes redirectAttributes) {
        notificacionService.marcarLeida(id);
        redirectAttributes.addFlashAttribute("successMsg",
                "Notificación marcada como leída.");
        return "redirect:/tutor-empresa/notificaciones";
    }

    @PostMapping("/notificaciones/leer-todas")
    public String marcarTodasLeidas(Principal principal,
                                    RedirectAttributes redirectAttributes) {
        TutorEmpresa tutor = getTutorEmpresaAutenticado(principal);
        notificacionService.marcarTodasLeidas(tutor);
        redirectAttributes.addFlashAttribute("successMsg",
                "Todas las notificaciones han sido marcadas como leídas.");
        return "redirect:/tutor-empresa/notificaciones";
    }


    // VALORACIÓN

    @GetMapping("/valoracion")
    public String valoracion(Model model, Principal principal) {
        TutorEmpresa tutor = getTutorEmpresaAutenticado(principal);
        cargarDatosBase(model, tutor);

        List<Asignacion> asignaciones = asignacionService.findActivasByTutorEmpresa(tutor);
        List<Alumno> alumnos = new ArrayList<>();
        List<Valoracion> valoraciones = new ArrayList<>();

        for (Asignacion asignacion : asignaciones) {
            Alumno alumno = asignacion.getAlumno();
            alumnos.add(alumno);

            valoracionService.findByAlumnoYTipo(alumno, TipoEvaluadorEnum.TUTOR_EMPRESA)
                    .ifPresent(valoraciones::add);
        }

        model.addAttribute("alumnos", alumnos);
        model.addAttribute("valoraciones", valoraciones);

        return "tutor-empresa/valoracion";
    }
    
    //REGISTRAR FALTA
    @PostMapping("/faltas/{idAlumno}/registrar")
    public String registrarFalta(@PathVariable Long idAlumno,
                                 @RequestParam("fechaFalta") String fechaFalta,
                                 @RequestParam("tipo") TipoFaltaEnum tipo,
                                 @RequestParam(value = "observacion", required = false) String observacion,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        try {
            TutorEmpresa tutor = getTutorEmpresaAutenticado(principal);
            Alumno alumno = getAlumnoAsignado(idAlumno, tutor);

            Asignacion asignacion = asignacionService.findAsignacionActiva(alumno)
                    .orElseThrow(() -> new RuntimeException(
                            "El alumno no tiene una asignación activa"));

            FaltaAsistencia falta = faltaAsistenciaService.registrar(
                    alumno,
                    asignacion,
                    tutor,
                    java.time.LocalDate.parse(fechaFalta),
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
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/tutor-empresa/faltas";
    }

    //EMITIR VALORACION-ALUMNO
    @PostMapping("/valoracion/{idAlumno}/emitir")
    public String emitirValoracionTutorEmpresa(@PathVariable Long idAlumno,
                                               @RequestParam(value = "actitud", required = false) Integer actitud,
                                               @RequestParam(value = "competencias", required = false) Integer competencias,
                                               @RequestParam(value = "integracion", required = false) Integer integracion,
                                               @RequestParam(value = "iniciativa", required = false) Integer iniciativa,
                                               @RequestParam(value = "observaciones", required = false) String observaciones,
                                               @RequestParam(value = "resultado", required = false) ResultadoEnum resultado,
                                               Principal principal,
                                               RedirectAttributes redirectAttributes) {
        try {
            TutorEmpresa tutor = getTutorEmpresaAutenticado(principal);
            Alumno alumno = getAlumnoAsignado(idAlumno, tutor);

            Valoracion valoracion = valoracionService
                    .findByAlumnoYTipo(alumno, TipoEvaluadorEnum.TUTOR_EMPRESA)
                    .orElseGet(() -> valoracionService.crear(
                            alumno, tutor, TipoEvaluadorEnum.TUTOR_EMPRESA));

            valoracionService.emitir(
                    valoracion.getId(),
                    actitud,
                    competencias,
                    integracion,
                    iniciativa,
                    observaciones,
                    resultado
            );

            redirectAttributes.addFlashAttribute("successMsg",
                    "Valoración emitida correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/tutor-empresa/valoracion";
    }

    //BLOQUEAR VALORACION-ALUMNO
    @PostMapping("/valoracion/{idValoracion}/bloquear")
    public String bloquearValoracionTutorEmpresa(@PathVariable Long idValoracion,
                                                 RedirectAttributes redirectAttributes) {
        try {
            valoracionService.bloquear(idValoracion);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Valoración bloqueada correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/tutor-empresa/valoracion";
    }


    // HELPERS PRIVADOS

    private TutorEmpresa getTutorEmpresaAutenticado(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("No hay usuario autenticado");
        }

        return usuarioService.findByNombreUsuario(principal.getName())
                .filter(usuario -> usuario instanceof TutorEmpresa)
                .map(usuario -> (TutorEmpresa) usuario)
                .orElseThrow(() -> new RuntimeException(
                        "Tutor de empresa no encontrado para el usuario autenticado: "
                                + principal.getName()));
    }

    private void cargarDatosBase(Model model, TutorEmpresa tutor) {
        model.addAttribute("tutorEmpresaActual", tutor);
        model.addAttribute("notificacionesNoLeidas",
                notificacionService.contarNoLeidas(tutor));
    }
    
    private Alumno getAlumnoAsignado(Long idAlumno, TutorEmpresa tutor) {
        List<Asignacion> asignaciones = asignacionService.findActivasByTutorEmpresa(tutor);

        return asignaciones.stream()
                .map(Asignacion::getAlumno)
                .filter(alumno -> alumno.getId().equals(idAlumno))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "El alumno no está asignado a este tutor de empresa"));
    }

}

