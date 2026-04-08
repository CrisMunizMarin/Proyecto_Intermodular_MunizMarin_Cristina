package com.mentorcore.controller;

import com.mentorcore.model.Alumno;
import com.mentorcore.model.Documento;
import com.mentorcore.model.Notificacion;
import com.mentorcore.model.Tarea;
import com.mentorcore.model.TutorCentro;
import com.mentorcore.model.Valoracion;
import com.mentorcore.model.FaltaAsistencia;
import com.mentorcore.model.enums.TipoEvaluadorEnum;
import com.mentorcore.model.enums.ResultadoEnum;
import com.mentorcore.service.AlumnoService;
import com.mentorcore.service.AsignacionService;
import com.mentorcore.service.DocumentoService;
import com.mentorcore.service.EmpresaService;
import com.mentorcore.service.InformeService;
import com.mentorcore.service.NotificacionService;
import com.mentorcore.service.TareaService;
import com.mentorcore.service.UsuarioService;
import com.mentorcore.service.ValoracionService;
import com.mentorcore.service.FaltaAsistenciaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
 * Controlador del panel del tutor de centro.
 * Gestiona la supervisión académica de alumnos, tareas, documentos,
 * informes, notificaciones y valoraciones.
 * RF4, RF5, RF6, RF7, RF8, RF13, RF18, RF21
 */
@Controller
@RequestMapping("/tutor-centro")
@RequiredArgsConstructor
@Slf4j
public class TutorCentroController {

    private final UsuarioService usuarioService;
    private final AlumnoService alumnoService;
    private final TareaService tareaService;
    private final DocumentoService documentoService;
    private final NotificacionService notificacionService;
    private final ValoracionService valoracionService;
    private final InformeService informeService;
    private final EmpresaService empresaService;
    private final AsignacionService asignacionService;
    private final FaltaAsistenciaService faltaAsistenciaService;



    // INICIO

    @GetMapping("/inicio")
    public String inicio(Model model, Principal principal) {
        TutorCentro tutor = getTutorCentroAutenticado(principal);
        cargarDatosBase(model, tutor);

        List<Alumno> alumnos = alumnoService.findByTutorCentro(tutor);

        model.addAttribute("totalAlumnos", alumnos.size());
        model.addAttribute("tareasPendientes",
                tareaService.findPendientesByTutorCentro(tutor.getId()));
        model.addAttribute("notificacionesNoLeidas",
                notificacionService.contarNoLeidas(tutor));

        return "tutor-centro/inicio";
    }


    // ALUMNOS

    @GetMapping("/alumnos")
    public String alumnos(Model model, Principal principal) {
        TutorCentro tutor = getTutorCentroAutenticado(principal);
        cargarDatosBase(model, tutor);

        model.addAttribute("alumnos", alumnoService.findByTutorCentro(tutor));

        return "tutor-centro/alumnos";
    }

    @GetMapping("/alumnos/{id}")
    public String detalleAlumno(@PathVariable Long id,
                                Model model,
                                Principal principal) {
        TutorCentro tutor = getTutorCentroAutenticado(principal);
        cargarDatosBase(model, tutor);

        Alumno alumno = getAlumnoDelTutor(id, tutor);

        model.addAttribute("alumno", alumno);
        model.addAttribute("tareas", tareaService.findByAlumno(alumno));
        model.addAttribute("documentos", documentoService.findByAlumno(alumno));
        model.addAttribute("valoraciones", valoracionService.findByAlumno(alumno));
        model.addAttribute("asignacionActiva",
                asignacionService.findAsignacionActiva(alumno).orElse(null));

        return "tutor-centro/detalle-alumno";
    }

    @GetMapping("/busqueda-alumno")
    public String busquedaAlumno(Model model, Principal principal) {
        TutorCentro tutor = getTutorCentroAutenticado(principal);
        cargarDatosBase(model, tutor);

        model.addAttribute("alumnos", alumnoService.findByTutorCentro(tutor));

        return "tutor-centro/busqueda-alumno";
    }


    // TAREAS

    @GetMapping("/tareas")
    public String tareas(Model model, Principal principal) {
        TutorCentro tutor = getTutorCentroAutenticado(principal);
        cargarDatosBase(model, tutor);

        model.addAttribute("tareasPendientes",
                tareaService.findPendientesByTutorCentro(tutor.getId()));

        return "tutor-centro/tareas";
    }


    // DOCUMENTOS

    @GetMapping("/documentos")
    public String documentos(Model model, Principal principal) {
        TutorCentro tutor = getTutorCentroAutenticado(principal);
        cargarDatosBase(model, tutor);

        List<Alumno> alumnos = alumnoService.findByTutorCentro(tutor);
        List<Documento> documentosPendientes = new ArrayList<>();

        for (Alumno alumno : alumnos) {
            documentosPendientes.addAll(documentoService.findPendientesByAlumno(alumno));
        }

        model.addAttribute("documentosPendientes", documentosPendientes);

        return "tutor-centro/documentos";
    }


    // NOTIFICACIONES

    @GetMapping("/notificaciones")
    public String notificaciones(Model model, Principal principal) {
        TutorCentro tutor = getTutorCentroAutenticado(principal);
        cargarDatosBase(model, tutor);

        List<Notificacion> notificaciones = notificacionService.findByReceptor(tutor);
        model.addAttribute("notificaciones", notificaciones);
        model.addAttribute("totalNoLeidas",
                notificacionService.contarNoLeidas(tutor));

        return "tutor-centro/notificaciones";
    }

    @PostMapping("/notificaciones/{id}/leer")
    public String marcarNotificacionLeida(@PathVariable Long id,
                                          RedirectAttributes redirectAttributes) {
        notificacionService.marcarLeida(id);
        redirectAttributes.addFlashAttribute("successMsg",
                "Notificación marcada como leída.");
        return "redirect:/tutor-centro/notificaciones";
    }

    @PostMapping("/notificaciones/leer-todas")
    public String marcarTodasLeidas(Principal principal,
                                    RedirectAttributes redirectAttributes) {
        TutorCentro tutor = getTutorCentroAutenticado(principal);
        notificacionService.marcarTodasLeidas(tutor);
        redirectAttributes.addFlashAttribute("successMsg",
                "Todas las notificaciones han sido marcadas como leídas.");
        return "redirect:/tutor-centro/notificaciones";
    }


    // EMPRESAS

    @GetMapping("/empresas")
    public String empresas(Model model, Principal principal) {
        TutorCentro tutor = getTutorCentroAutenticado(principal);
        cargarDatosBase(model, tutor);

        model.addAttribute("empresas", empresaService.findActivas());

        return "tutor-centro/empresas";
    }


    // ASIGNACIONES

    @GetMapping("/asignaciones")
    public String asignaciones(Model model, Principal principal) {
        TutorCentro tutor = getTutorCentroAutenticado(principal);
        cargarDatosBase(model, tutor);

        List<Alumno> alumnos = alumnoService.findByTutorCentro(tutor);
        model.addAttribute("alumnos", alumnos);

        return "tutor-centro/asignaciones";
    }


    // INFORMES

    @GetMapping("/informes")
    public String informes(Model model, Principal principal) {
        TutorCentro tutor = getTutorCentroAutenticado(principal);
        cargarDatosBase(model, tutor);

        model.addAttribute("alumnos", alumnoService.findByTutorCentro(tutor));

        return "tutor-centro/informes";
    }

    @GetMapping("/informes/{idAlumno}/pdf")
    public ResponseEntity<byte[]> descargarInformePdf(@PathVariable Long idAlumno,
                                                      Principal principal) {
        TutorCentro tutor = getTutorCentroAutenticado(principal);
        Alumno alumno = getAlumnoDelTutor(idAlumno, tutor);

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


    // VALORACIÓN

    @GetMapping("/valoracion")
    public String valoracion(Model model, Principal principal) {
        TutorCentro tutor = getTutorCentroAutenticado(principal);
        cargarDatosBase(model, tutor);

        List<Alumno> alumnos = alumnoService.findByTutorCentro(tutor);
        List<Valoracion> valoracionesTutorCentro = new ArrayList<>();

        for (Alumno alumno : alumnos) {
            valoracionService.findByAlumnoYTipo(alumno, TipoEvaluadorEnum.TUTOR_CENTRO)
                    .ifPresent(valoracionesTutorCentro::add);
        }

        model.addAttribute("alumnos", alumnos);
        model.addAttribute("valoraciones", valoracionesTutorCentro);

        return "tutor-centro/valoracion";
    }


    //VALIDAR TAREAS
    @PostMapping("/tareas/{id}/validar")
    public String validarTarea(@PathVariable Long id,
                               @RequestParam(value = "comentario", required = false) String comentario,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        try {
            TutorCentro tutor = getTutorCentroAutenticado(principal);
            Tarea tarea = tareaService.findById(id)
                    .orElseThrow(() -> new RuntimeException(
                            "Tarea no encontrada con id: " + id));

            Alumno alumno = tarea.getAlumno();
            if (alumno.getTutorCentro() == null ||
                    !alumno.getTutorCentro().getId().equals(tutor.getId())) {
                throw new RuntimeException("No puedes validar tareas de un alumno no asignado");
            }

            tareaService.validar(id, tutor, comentario);
            notificacionService.notificarRevisionTarea(alumno, true, comentario);

            redirectAttributes.addFlashAttribute("successMsg",
                    "Tarea validada correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/tutor-centro/tareas";
    }
    

    //RECHAZAR TAREAS
    @PostMapping("/tareas/{id}/rechazar")
    public String rechazarTarea(@PathVariable Long id,
                                @RequestParam(value = "comentario", required = false) String comentario,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        try {
            TutorCentro tutor = getTutorCentroAutenticado(principal);
            Tarea tarea = tareaService.findById(id)
                    .orElseThrow(() -> new RuntimeException(
                            "Tarea no encontrada con id: " + id));

            Alumno alumno = tarea.getAlumno();
            if (alumno.getTutorCentro() == null ||
                    !alumno.getTutorCentro().getId().equals(tutor.getId())) {
                throw new RuntimeException("No puedes revisar tareas de un alumno no asignado");
            }

            tareaService.rechazar(id, tutor, comentario);
            notificacionService.notificarRevisionTarea(alumno, false, comentario);

            redirectAttributes.addFlashAttribute("successMsg",
                    "Tarea rechazada correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/tutor-centro/tareas";
    }

    //REVISAR TAREAS
    @PostMapping("/tareas/{id}/revision")
    public String marcarTareaRequiereRevision(@PathVariable Long id,
                                              @RequestParam(value = "comentario", required = false) String comentario,
                                              Principal principal,
                                              RedirectAttributes redirectAttributes) {
        try {
            TutorCentro tutor = getTutorCentroAutenticado(principal);
            Tarea tarea = tareaService.findById(id)
                    .orElseThrow(() -> new RuntimeException(
                            "Tarea no encontrada con id: " + id));

            Alumno alumno = tarea.getAlumno();
            if (alumno.getTutorCentro() == null ||
                    !alumno.getTutorCentro().getId().equals(tutor.getId())) {
                throw new RuntimeException("No puedes revisar tareas de un alumno no asignado");
            }

            tareaService.marcarRequiereRevision(id, tutor, comentario);
            notificacionService.notificarRevisionTarea(alumno, false, comentario);

            redirectAttributes.addFlashAttribute("successMsg",
                    "La tarea se ha devuelto para revisión.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/tutor-centro/tareas";
    }

    //VALIDAR DOCUMENTOS
    @PostMapping("/documentos/{id}/validar")
    public String validarDocumento(@PathVariable Long id,
                                   @RequestParam(value = "comentario", required = false) String comentario,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {
        try {
            TutorCentro tutor = getTutorCentroAutenticado(principal);
            Documento documento = documentoService.findById(id)
                    .orElseThrow(() -> new RuntimeException(
                            "Documento no encontrado con id: " + id));

            Alumno alumno = documento.getAlumno();
            if (alumno.getTutorCentro() == null ||
                    !alumno.getTutorCentro().getId().equals(tutor.getId())) {
                throw new RuntimeException("No puedes validar documentos de un alumno no asignado");
            }

            documentoService.validar(id, comentario);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Documento validado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/tutor-centro/documentos";
    }

    //RECHAZAR DOCUMENTOS
    @PostMapping("/documentos/{id}/rechazar")
    public String rechazarDocumento(@PathVariable Long id,
                                    @RequestParam(value = "motivo", required = false) String motivo,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        try {
            TutorCentro tutor = getTutorCentroAutenticado(principal);
            Documento documento = documentoService.findById(id)
                    .orElseThrow(() -> new RuntimeException(
                            "Documento no encontrado con id: " + id));

            Alumno alumno = documento.getAlumno();
            if (alumno.getTutorCentro() == null ||
                    !alumno.getTutorCentro().getId().equals(tutor.getId())) {
                throw new RuntimeException("No puedes revisar documentos de un alumno no asignado");
            }

            documentoService.rechazar(id, motivo);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Documento rechazado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/tutor-centro/documentos";
    }
    

    //EMISION DE VALORACION-ALUMNO
    @PostMapping("/valoracion/{idAlumno}/emitir")
    public String emitirValoracionTutorCentro(@PathVariable Long idAlumno,
                                              @RequestParam(value = "actitud", required = false) Integer actitud,
                                              @RequestParam(value = "competencias", required = false) Integer competencias,
                                              @RequestParam(value = "integracion", required = false) Integer integracion,
                                              @RequestParam(value = "iniciativa", required = false) Integer iniciativa,
                                              @RequestParam(value = "observaciones", required = false) String observaciones,
                                              @RequestParam(value = "resultado", required = false) ResultadoEnum resultado,
                                              Principal principal,
                                              RedirectAttributes redirectAttributes) {
        try {
            TutorCentro tutor = getTutorCentroAutenticado(principal);
            Alumno alumno = getAlumnoDelTutor(idAlumno, tutor);

            Valoracion valoracion = valoracionService
                    .findByAlumnoYTipo(alumno, TipoEvaluadorEnum.TUTOR_CENTRO)
                    .orElseGet(() -> valoracionService.crear(
                            alumno, tutor, TipoEvaluadorEnum.TUTOR_CENTRO));

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

        return "redirect:/tutor-centro/valoracion";
    }
    

    //BLOQUEO DE VALORACION-ALUMNO
    @PostMapping("/valoracion/{idValoracion}/bloquear")
    public String bloquearValoracionTutorCentro(@PathVariable Long idValoracion,
                                                RedirectAttributes redirectAttributes) {
        try {
            valoracionService.bloquear(idValoracion);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Valoración bloqueada correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/tutor-centro/valoracion";
    }
    
    //APROBAR FALTA ASISTENCIA
    @PostMapping("/faltas/{idFalta}/aprobar")
    public String aprobarJustificante(@PathVariable Long idFalta,
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
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/tutor-centro/documentos";
    }

    //DENEGAR FALTA ASISTENCIA
    @PostMapping("/faltas/{idFalta}/denegar")
    public String denegarJustificante(@PathVariable Long idFalta,
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
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/tutor-centro/documentos";
    }


    
    // HELPERS PRIVADOS

    private TutorCentro getTutorCentroAutenticado(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("No hay usuario autenticado");
        }

        return usuarioService.findByNombreUsuario(principal.getName())
                .filter(usuario -> usuario instanceof TutorCentro)
                .map(usuario -> (TutorCentro) usuario)
                .orElseThrow(() -> new RuntimeException(
                        "Tutor de centro no encontrado para el usuario autenticado: "
                                + principal.getName()));
    }

    private Alumno getAlumnoDelTutor(Long idAlumno, TutorCentro tutor) {
        Alumno alumno = alumnoService.findById(idAlumno)
                .orElseThrow(() -> new RuntimeException(
                        "Alumno no encontrado con id: " + idAlumno));

        if (alumno.getTutorCentro() == null ||
                !alumno.getTutorCentro().getId().equals(tutor.getId())) {
            throw new RuntimeException("Acceso no permitido al alumno solicitado");
        }

        return alumno;
    }

    private void cargarDatosBase(Model model, TutorCentro tutor) {
        model.addAttribute("tutorCentroActual", tutor);
        model.addAttribute("notificacionesNoLeidas",
                notificacionService.contarNoLeidas(tutor));
    }
}

