package com.mentorcore.controller;

import com.mentorcore.model.Alumno;
import com.mentorcore.model.Notificacion;
import com.mentorcore.model.Valoracion;
import com.mentorcore.model.Tarea;
import com.mentorcore.model.Documento;
import com.mentorcore.model.TipoDocumento;
import com.mentorcore.model.FaltaAsistencia;
import com.mentorcore.model.enums.ContextoDocumentoEnum;
import com.mentorcore.service.AlumnoService;
import com.mentorcore.service.DocumentoService;
import com.mentorcore.service.FaltaAsistenciaService;
import com.mentorcore.service.NotificacionService;
import com.mentorcore.service.TareaService;
import com.mentorcore.service.ValoracionService;
import com.mentorcore.service.EmailService;
import com.mentorcore.service.TipoDocumentoService;
import com.mentorcore.util.ControllerMessageUtil;
import com.mentorcore.util.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Controlador del panel del alumno.
 * Gestiona el acceso a sus vistas principales: progreso, tareas,
 * documentos, faltas, notificaciones y valoración.
 * RF2, RF3, RF7, RF16, RF22
 */
@Controller
@RequestMapping("/alumno")
@RequiredArgsConstructor
@Slf4j

public class AlumnoController {

    private final AlumnoService alumnoService;
    private final TareaService tareaService;
    private final DocumentoService documentoService;
    private final FaltaAsistenciaService faltaAsistenciaService;
    private final NotificacionService notificacionService;
    private final ValoracionService valoracionService;
    private final EmailService emailService;
    private final TipoDocumentoService tipoDocumentoService;
    private final FileUploadUtil fileUploadUtil;

    @Value("${mentorcore.uploads.ruta-base}")
    private String rutaBaseUploads;



    // INICIO

    @GetMapping("/inicio")
    public String inicio(Model model, Principal principal) {
        Alumno alumno = getAlumnoAutenticado(principal);
        cargarDatosBase(model, alumno);

        model.addAttribute("seccionActiva", "inicio");
        model.addAttribute("porcentajeCompletado",
                alumnoService.getPorcentajeCompletado(alumno.getId()));
        model.addAttribute("horasCompletadas", alumno.getHorasCompletadas());
        model.addAttribute("horasTotales", alumno.getHorasTotalesFe());
        model.addAttribute("tareasPendientes", tareaService.findPendientesByAlumno(alumno));
        model.addAttribute("notificacionesNoLeidas",
                notificacionService.contarNoLeidas(alumno));

        return "alumno/inicio";
    }


    // PROGRESO

    @GetMapping("/progreso")
    public String progreso(Model model, Principal principal) {
        Alumno alumno = getAlumnoAutenticado(principal);
        cargarDatosBase(model, alumno);

        model.addAttribute("seccionActiva", "progreso");
        model.addAttribute("porcentajeCompletado",
                alumnoService.getPorcentajeCompletado(alumno.getId()));
        model.addAttribute("haCompletadoHoras",
                alumnoService.haCompletadoHoras(alumno.getId()));
        model.addAttribute("tareasValidadas", tareaService.contarValidadas(alumno));
        model.addAttribute("tareasPendientes", tareaService.contarPendientes(alumno));
        model.addAttribute("tareasRechazadas", tareaService.contarRechazadas(alumno));

        return "alumno/progreso";
    }


    // TAREAS

    @GetMapping("/tareas")
    public String tareas(Model model, Principal principal) {
        Alumno alumno = getAlumnoAutenticado(principal);
        cargarDatosBase(model, alumno);

        model.addAttribute("seccionActiva", "tareas");
        model.addAttribute("tareas", tareaService.findByAlumno(alumno));

        return "alumno/tareas";
    }

    @GetMapping("/tareas/nueva")
    public String mostrarNuevaTarea(Model model, Principal principal) {
        Alumno alumno = getAlumnoAutenticado(principal);
        cargarDatosBase(model, alumno);

        model.addAttribute("seccionActiva", "tareas");

        return "alumno/nueva-tarea";
    }


    // DOCUMENTOS

    @GetMapping("/documentos")
    public String documentos(Model model, Principal principal) {
        Alumno alumno = getAlumnoAutenticado(principal);
        cargarDatosBase(model, alumno);

        model.addAttribute("seccionActiva", "documentos");
        model.addAttribute("documentos", documentoService.findByAlumno(alumno));
        model.addAttribute("documentosPendientes",
                documentoService.findPendientesByAlumno(alumno));
        model.addAttribute("tiposDocumento", tipoDocumentoService.findActivos());

        return "alumno/documentos";
    }


    @PostMapping("/documentos/subir")
    public String subirDocumento(@RequestParam("idTipoDocumento") Long idTipoDocumento,
                                 @RequestParam("archivo") MultipartFile archivo,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        try {
            Alumno alumno = getAlumnoAutenticado(principal);

            TipoDocumento tipoDocumento = tipoDocumentoService.findById(idTipoDocumento)
                    .orElseThrow(() -> new RuntimeException(
                            "Tipo de documento no encontrado con id: " + idTipoDocumento));

            if (archivo == null || archivo.isEmpty()) {
                throw new IllegalArgumentException("Debes seleccionar un archivo");
            }

            String extension = "";
            String nombreOriginal = archivo.getOriginalFilename();
            if (nombreOriginal != null && nombreOriginal.contains(".")) {
                extension = nombreOriginal.substring(nombreOriginal.lastIndexOf(".") + 1).toLowerCase();
            }

            if (!tipoDocumento.isExtensionValida(extension)) {
                throw new IllegalArgumentException(
                        "La extensión del archivo no está permitida para este tipo de documento");
            }

            String subcarpeta = "alumnos/" + alumno.getId() + "/documentos";
            String rutaGuardada = fileUploadUtil.guardarArchivo(
                    rutaBaseUploads,
                    subcarpeta,
                    archivo
            );

            Documento documento = documentoService.subirDocumento(
                    alumno,
                    tipoDocumento,
                    alumno,
                    archivo.getOriginalFilename(),
                    rutaGuardada,
                    archivo.getContentType(),
                    archivo.getSize(),
                    tipoDocumento.isEsObligatorio(),
                    ContextoDocumentoEnum.EXPEDIENTE
            );

            redirectAttributes.addFlashAttribute("successMsg",
                    "Documento subido correctamente.");
        } catch (Exception e) {
            ControllerMessageUtil.addSafeErrorMessage(
                    redirectAttributes,
                    log,
                    "Error al subir documento del alumno",
                    e,
                    "No se pudo subir el documento. Inténtalo de nuevo."
            );
        }

        return "redirect:/alumno/documentos";
    }


    // FALTAS

    @GetMapping("/faltas")
    public String faltas(Model model, Principal principal) {
        Alumno alumno = getAlumnoAutenticado(principal);
        cargarDatosBase(model, alumno);

        model.addAttribute("seccionActiva", "faltas");
        model.addAttribute("faltas", faltaAsistenciaService.findByAlumno(alumno));
        model.addAttribute("faltasJustificadas",
                faltaAsistenciaService.contarJustificadas(alumno));
        model.addAttribute("faltasInjustificadas",
                faltaAsistenciaService.contarInjustificadas(alumno));

        return "alumno/faltas";
    }
    
    
    //JUSTIFICAR FALTA
    @PostMapping("/faltas/{idFalta}/justificar")
    public String adjuntarJustificante(@PathVariable Long idFalta,
                                       @RequestParam("archivo") MultipartFile archivo,
                                       Principal principal,
                                       RedirectAttributes redirectAttributes) {
        try {
            Alumno alumno = getAlumnoAutenticado(principal);

            FaltaAsistencia falta = faltaAsistenciaService.findById(idFalta)
                    .orElseThrow(() -> new RuntimeException(
                            "Falta no encontrada con id: " + idFalta));

            if (!falta.getAlumno().getId().equals(alumno.getId())) {
                throw new RuntimeException("No puedes justificar una falta que no es tuya");
            }

            if (archivo == null || archivo.isEmpty()) {
                throw new IllegalArgumentException("Debes seleccionar un archivo");
            }

            TipoDocumento tipoDocumento = tipoDocumentoService
                    .findByNombreActivo("Justificante de Falta")
                    .orElseThrow(() -> new RuntimeException(
                            "No existe un tipo de documento activo llamado 'Justificante de Falta'"));

            String extension = "";
            String nombreOriginal = archivo.getOriginalFilename();
            if (nombreOriginal != null && nombreOriginal.contains(".")) {
                extension = nombreOriginal.substring(nombreOriginal.lastIndexOf(".") + 1).toLowerCase();
            }

            if (!tipoDocumento.isExtensionValida(extension)) {
                throw new IllegalArgumentException(
                        "La extensión del archivo no está permitida para justificantes");
            }

            String subcarpeta = "alumnos/" + alumno.getId() + "/justificantes";
            String rutaGuardada = fileUploadUtil.guardarArchivo(
                    rutaBaseUploads,
                    subcarpeta,
                    archivo
            );

            Documento documento = documentoService.subirDocumento(
                    alumno,
                    tipoDocumento,
                    alumno,
                    archivo.getOriginalFilename(),
                    rutaGuardada,
                    archivo.getContentType(),
                    archivo.getSize(),
                    false,
                    ContextoDocumentoEnum.JUSTIFICANTE_FALTA
            );

            faltaAsistenciaService.adjuntarJustificante(idFalta, documento);

            if (alumno.getTutorCentro() != null) {
                notificacionService.notificarJustificante(
                        alumno.getTutorCentro(),
                        alumno.getNombreCompleto(),
                        falta.getFechaFalta().toString()
                );
            }

            redirectAttributes.addFlashAttribute("successMsg",
                    "Justificante adjuntado correctamente.");
        } catch (Exception e) {
            ControllerMessageUtil.addSafeErrorMessage(
                    redirectAttributes,
                    log,
                    "Error al adjuntar justificante de falta",
                    e,
                    "No se pudo adjuntar el justificante. Inténtalo de nuevo."
            );
        }

        return "redirect:/alumno/faltas";
    }



    // NOTIFICACIONES

    @GetMapping("/notificaciones")
    public String notificaciones(Model model, Principal principal) {
        Alumno alumno = getAlumnoAutenticado(principal);
        cargarDatosBase(model, alumno);

        List<Notificacion> notificaciones = notificacionService.findByReceptor(alumno);
        model.addAttribute("notificaciones", notificaciones);
        model.addAttribute("totalNoLeidas",
                notificacionService.contarNoLeidas(alumno));
        model.addAttribute("seccionActiva", "notificaciones");


        return "alumno/notificaciones";
    }

    @PostMapping("/notificaciones/{id}/leer")
    public String marcarNotificacionLeida(@PathVariable Long id,
                                          RedirectAttributes redirectAttributes) {
        notificacionService.marcarLeida(id);
        redirectAttributes.addFlashAttribute("successMsg",
                "Notificación marcada como leída.");
        return "redirect:/alumno/notificaciones";
    }

    @PostMapping("/notificaciones/leer-todas")
    public String marcarTodasLeidas(Principal principal,
                                    RedirectAttributes redirectAttributes) {
        Alumno alumno = getAlumnoAutenticado(principal);
        notificacionService.marcarTodasLeidas(alumno);
        redirectAttributes.addFlashAttribute("successMsg",
                "Todas las notificaciones han sido marcadas como leídas.");
        return "redirect:/alumno/notificaciones";
    }


    // VALORACIÓN

    @GetMapping("/valoracion")
    public String valoracion(Model model, Principal principal) {
        Alumno alumno = getAlumnoAutenticado(principal);
        cargarDatosBase(model, alumno);

        List<Valoracion> valoraciones = valoracionService.findByAlumno(alumno);
        model.addAttribute("valoraciones", valoraciones);
        model.addAttribute("seccionActiva", "valoracion");


        return "alumno/valoracion";
    }

    //CREAR TAREA
    @PostMapping("/tareas/nueva")
    public String registrarTarea(@RequestParam("fechaRegistro") String fechaRegistro,
                                 @RequestParam("descripcion") String descripcion,
                                 @RequestParam("horasDedicadas") BigDecimal horasDedicadas,
                                 @RequestParam(value = "areaActividad", required = false) String areaActividad,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        try {
            Alumno alumno = getAlumnoAutenticado(principal);

            Tarea tarea = new Tarea();
            tarea.setAlumno(alumno);
            tarea.setFechaRegistro(LocalDate.parse(fechaRegistro));
            tarea.setDescripcion(descripcion);
            tarea.setHorasDedicadas(horasDedicadas);
            tarea.setAreaActividad(areaActividad);

            tareaService.registrar(tarea);
            String mensajeExito = "Tarea registrada correctamente.";

            if (alumno.getTutorCentro() != null) {
                try {
                    notificacionService.notificarNuevaTarea(
                            alumno.getTutorCentro(),
                            alumno.getNombreCompleto()
                    );

                    if (alumno.getTutorCentro().getEmail() != null &&
                            !alumno.getTutorCentro().getEmail().isBlank()) {
                        emailService.notificarNuevaTarea(
                                alumno.getTutorCentro().getEmail(),
                                alumno.getNombreCompleto(),
                                fechaRegistro
                        );
                    }
                } catch (Exception e) {
                    log.warn("La tarea se registró, pero falló la notificación al tutor centro para el alumno '{}': {}",
                            alumno.getNombreUsuario(), e.getMessage(), e);
                    mensajeExito = "Tarea registrada correctamente. La notificación al tutor se enviará más tarde.";
                }
            }

            redirectAttributes.addFlashAttribute("successMsg", mensajeExito);
        } catch (Exception e) {
            ControllerMessageUtil.addSafeErrorMessage(
                    redirectAttributes,
                    log,
                    "Error al registrar tarea del alumno",
                    e,
                    "No se pudo registrar la tarea. Inténtalo de nuevo."
            );
            return "redirect:/alumno/tareas/nueva";
        }

        return "redirect:/alumno/tareas";
    }


    // HELPERS PRIVADOS

    private Alumno getAlumnoAutenticado(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("No hay usuario autenticado");
        }

        return alumnoService.findByNombreUsuario(principal.getName())
                .orElseThrow(() -> new RuntimeException(
                        "Alumno no encontrado para el usuario autenticado: " + principal.getName()));
    }

    private void cargarDatosBase(Model model, Alumno alumno) {
        model.addAttribute("alumnoActual", alumno);
        model.addAttribute("notificacionesNoLeidas",
                notificacionService.contarNoLeidas(alumno));
    }
}
