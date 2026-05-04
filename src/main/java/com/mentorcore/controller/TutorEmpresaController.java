package com.mentorcore.controller;

import com.mentorcore.model.Alumno;
import com.mentorcore.model.Asignacion;
import com.mentorcore.model.Convenio;
import com.mentorcore.model.Documento;
import com.mentorcore.model.Notificacion;
import com.mentorcore.model.TipoDocumento;
import com.mentorcore.model.TutorEmpresa;
import com.mentorcore.model.Valoracion;
import com.mentorcore.model.FaltaAsistencia;
import com.mentorcore.model.Tarea;
import com.mentorcore.model.enums.ContextoDocumentoEnum;
import com.mentorcore.model.enums.ResultadoEnum;
import com.mentorcore.model.enums.TipoFaltaEnum;
import com.mentorcore.model.enums.TipoEvaluadorEnum;
import com.mentorcore.model.enums.EstadoFaltaEnum;
import com.mentorcore.model.enums.EstadoValidacionEnum;
import com.mentorcore.model.enums.TipoNotificacionEnum;
import com.mentorcore.service.AsignacionService;
import com.mentorcore.service.ConvenioService;
import com.mentorcore.service.DocumentoService;
import com.mentorcore.service.FaltaAsistenciaService;
import com.mentorcore.service.NotificacionService;
import com.mentorcore.service.TareaService;
import com.mentorcore.service.TipoDocumentoService;
import com.mentorcore.service.TutorEmpresaService;
import com.mentorcore.service.ValoracionService;
import com.mentorcore.util.ControllerMessageUtil;
import com.mentorcore.util.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
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

    private final TutorEmpresaService tutorEmpresaService;
    private final AsignacionService asignacionService;
    private final TareaService tareaService;
    private final FaltaAsistenciaService faltaAsistenciaService;
    private final DocumentoService documentoService;
    private final ConvenioService convenioService;
    private final NotificacionService notificacionService;
    private final ValoracionService valoracionService;
    private final TipoDocumentoService tipoDocumentoService;
    private final FileUploadUtil fileUploadUtil;

    @Value("${mentorcore.uploads.ruta-base}")
    private String rutaBaseUploads;


    // INICIO

    @GetMapping("/inicio")
    public String inicio(Model model, Principal principal) {
        TutorEmpresa tutor = getTutorEmpresaAutenticado(principal);
        cargarDatosBase(model, tutor);

        List<Asignacion> asignacionesActivas = asignacionService.findActivasByTutorEmpresaId(tutor.getId());

        model.addAttribute("seccionActiva", "inicio");
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

        List<Asignacion> asignaciones = asignacionService.findActivasByTutorEmpresaId(tutor.getId());
        model.addAttribute("seccionActiva", "busqueda-alumno");
        model.addAttribute("asignaciones", asignaciones);

        return "tutor-empresa/busqueda-alumno";
    }

    @GetMapping("/alumnos/{id}")
    public String detalleAlumno(@PathVariable Long id,
                                Model model,
                                Principal principal) {
        TutorEmpresa tutor = getTutorEmpresaAutenticado(principal);
        cargarDatosBase(model, tutor);

        Alumno alumno = getAlumnoAsignado(id, tutor);
        Asignacion asignacionActiva = asignacionService.findAsignacionActiva(alumno)
                .orElse(null);

        model.addAttribute("seccionActiva", "busqueda-alumno");
        model.addAttribute("alumno", alumno);
        model.addAttribute("asignacionActiva", asignacionActiva);
        model.addAttribute("tareas", tareaService.findByAlumno(alumno));
        model.addAttribute("faltas", faltaAsistenciaService.findByAlumno(alumno));
        model.addAttribute("documentos", documentoService.findByAlumno(alumno));
        model.addAttribute("valoraciones", valoracionService.findByAlumno(alumno));

        return "tutor-empresa/detalle-alumno";
    }


    // TAREAS

    @GetMapping("/tareas")
    public String tareas(Model model, Principal principal) {
        TutorEmpresa tutor = getTutorEmpresaAutenticado(principal);
        cargarDatosBase(model, tutor);

        List<Asignacion> asignaciones = asignacionService.findActivasByTutorEmpresaId(tutor.getId());
        List<Tarea> tareas = new ArrayList<>();

        for (Asignacion asignacion : asignaciones) {
            tareas.addAll(tareaService.findByAlumno(asignacion.getAlumno()));
        }

        tareas.sort(Comparator
                .comparing(Tarea::getFechaRegistro, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Tarea::getFechaCreacion, Comparator.nullsLast(Comparator.reverseOrder())));

        model.addAttribute("seccionActiva", "tareas");
        model.addAttribute("tareas", tareas);
        model.addAttribute("tareasPendientesCentro", tareas.stream()
                .filter(tarea -> tarea.getEstadoValidacion() == EstadoValidacionEnum.PENDIENTE)
                .count());
        model.addAttribute("tareasConSeguimientoEmpresa", tareas.stream()
                .filter(tarea -> tarea.getValoracionTutorEmpresa() != null
                        || (tarea.getComentarioTutorEmpresa() != null
                        && !tarea.getComentarioTutorEmpresa().isBlank()))
                .count());

        return "tutor-empresa/tareas";
    }

    @PostMapping("/tareas/{idTarea}/seguimiento")
    public String registrarSeguimientoTarea(@PathVariable Long idTarea,
                                            @RequestParam(value = "valoracionEmpresa", required = false) Integer valoracionEmpresa,
                                            @RequestParam(value = "comentarioEmpresa", required = false) String comentarioEmpresa,
                                            @RequestParam(value = "idAlumno", required = false) Long idAlumno,
                                            @RequestParam(value = "volverA", required = false) String volverA,
                                            Principal principal,
                                            RedirectAttributes redirectAttributes) {
        try {
            TutorEmpresa tutor = getTutorEmpresaAutenticado(principal);
            Tarea tarea = tareaService.findDetalleById(idTarea)
                    .orElseThrow(() -> new RuntimeException(
                            "No se ha encontrado la tarea seleccionada"));

            Alumno alumno = getAlumnoAsignado(tarea.getAlumno().getId(), tutor);
            tareaService.registrarSeguimientoEmpresa(idTarea, valoracionEmpresa, comentarioEmpresa);

            redirectAttributes.addFlashAttribute("successMsg",
                    "Seguimiento de tarea guardado correctamente.");

            if ("detalle".equals(volverA) && idAlumno != null && alumno.getId().equals(idAlumno)) {
                return "redirect:/tutor-empresa/alumnos/" + idAlumno;
            }
        } catch (Exception e) {
            ControllerMessageUtil.addSafeErrorMessage(
                    redirectAttributes,
                    log,
                    "Error al registrar seguimiento de tarea desde tutor de empresa",
                    e,
                    "No se pudo guardar el seguimiento de la tarea. Inténtalo de nuevo."
            );

            if ("detalle".equals(volverA) && idAlumno != null) {
                return "redirect:/tutor-empresa/alumnos/" + idAlumno;
            }
        }

        return "redirect:/tutor-empresa/tareas";
    }


    // DOCUMENTOS

    @GetMapping("/documentos")
    public String documentos(Model model, Principal principal) {
        TutorEmpresa tutor = getTutorEmpresaAutenticado(principal);
        cargarDatosBase(model, tutor);

        List<Asignacion> asignaciones = asignacionService.findActivasByTutorEmpresaId(tutor.getId());
        List<DocumentacionAlumnoView> documentacion = new ArrayList<>();

        for (Asignacion asignacion : asignaciones) {
            Alumno alumno = asignacion.getAlumno();
            documentacion.add(new DocumentacionAlumnoView(
                    alumno.getId(),
                    alumno.getNombreCompleto(),
                    alumno.getNombreUsuario(),
                    alumno.getGrupo(),
                    asignacion.getEmpresa() != null ? asignacion.getEmpresa().getNombre() : "—",
                    documentoService.findByAlumno(alumno),
                    convenioService.findByAlumno(alumno)
            ));
        }

        model.addAttribute("seccionActiva", "documentos");
        model.addAttribute("documentacion", documentacion);
        model.addAttribute("tiposDocumentoEmpresa",
                tipoDocumentoService.findActivosPorRoles("TUTOR_EMPRESA", "TODOS"));

        return "tutor-empresa/documentos";
    }

    @PostMapping("/documentos/subir")
    public String subirDocumentoEmpresa(@RequestParam("idAlumno") Long idAlumno,
                                        @RequestParam("idTipoDocumento") Long idTipoDocumento,
                                        @RequestParam("archivo") MultipartFile archivo,
                                        Principal principal,
                                        RedirectAttributes redirectAttributes) {
        try {
            TutorEmpresa tutor = getTutorEmpresaAutenticado(principal);
            Alumno alumno = getAlumnoAsignado(idAlumno, tutor);

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

            String subcarpeta = "alumnos/" + alumno.getId() + "/empresa";
            String rutaGuardada = fileUploadUtil.guardarArchivo(
                    rutaBaseUploads,
                    subcarpeta,
                    archivo
            );

            documentoService.subirDocumento(
                    alumno,
                    tipoDocumento,
                    tutor,
                    archivo.getOriginalFilename(),
                    rutaGuardada,
                    archivo.getContentType(),
                    archivo.getSize(),
                    false,
                    ContextoDocumentoEnum.EXPEDIENTE
            );

            if (alumno.getTutorCentro() != null) {
                notificacionService.enviarSistema(
                        alumno.getTutorCentro(),
                        TipoNotificacionEnum.AVISO,
                        "Nuevo documento subido por empresa",
                        "El tutor de empresa ha subido un documento para " + alumno.getNombreCompleto() + "."
                );
            }

            redirectAttributes.addFlashAttribute("successMsg",
                    "Documento de empresa subido correctamente.");
        } catch (Exception e) {
            ControllerMessageUtil.addSafeErrorMessage(
                    redirectAttributes,
                    log,
                    "Error al subir documento desde tutor de empresa",
                    e,
                    "No se pudo subir el documento. Inténtalo de nuevo."
            );
        }

        return "redirect:/tutor-empresa/documentos";
    }


    // FALTAS

    @GetMapping("/faltas")
    public String faltas(Model model, Principal principal) {
        TutorEmpresa tutor = getTutorEmpresaAutenticado(principal);
        cargarDatosBase(model, tutor);

        List<Asignacion> asignaciones = asignacionService.findActivasByTutorEmpresaId(tutor.getId());
        List<FaltaAsistencia> faltasRegistradas = new ArrayList<>();

        for (Asignacion asignacion : asignaciones) {
            Alumno alumno = asignacion.getAlumno();
            faltasRegistradas.addAll(faltaAsistenciaService.findByAlumno(alumno));
        }

        List<FaltaAsistencia> faltasPendientesEmpresa = faltasRegistradas.stream()
                .filter(faltaAsistenciaService::requiereVerificacionEmpresa)
                .sorted(Comparator
                        .comparing(FaltaAsistencia::getFechaFalta, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(FaltaAsistencia::getFechaCreacion, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        model.addAttribute("seccionActiva", "faltas");
        model.addAttribute("asignaciones", asignaciones);
        model.addAttribute("faltasRegistradas", faltasRegistradas);
        model.addAttribute("faltasPendientesEmpresa", faltasPendientesEmpresa);
        model.addAttribute("faltasPendientesRevision", faltasPendientesEmpresa.size());

        return "tutor-empresa/faltas";
    }


    // NOTIFICACIONES

    @GetMapping("/notificaciones")
    public String notificaciones(Model model, Principal principal) {
        TutorEmpresa tutor = getTutorEmpresaAutenticado(principal);
        cargarDatosBase(model, tutor);

        List<Notificacion> notificaciones = notificacionService.findByReceptor(tutor);
        model.addAttribute("seccionActiva", "notificaciones");
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

        List<Asignacion> asignaciones = asignacionService.findActivasByTutorEmpresaId(tutor.getId());
        List<Alumno> alumnos = new ArrayList<>();
        List<Valoracion> valoraciones = new ArrayList<>();

        for (Asignacion asignacion : asignaciones) {
            Alumno alumno = asignacion.getAlumno();
            alumnos.add(alumno);

            valoracionService.findByAlumnoYTipo(alumno, TipoEvaluadorEnum.TUTOR_EMPRESA)
                    .ifPresent(valoraciones::add);
        }

        model.addAttribute("seccionActiva", "valoracion");
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
            LocalDate fecha = LocalDate.parse(fechaFalta);

            Asignacion asignacion = asignacionService.findAsignacionActiva(alumno)
                    .orElseThrow(() -> new RuntimeException(
                            "El alumno no tiene una asignación activa"));

            FaltaAsistencia falta = faltaAsistenciaService.registrar(
                    alumno,
                    asignacion,
                    tutor,
                    fecha,
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
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            ControllerMessageUtil.addSafeErrorMessage(
                    redirectAttributes,
                    log,
                    "Error de integridad al registrar falta desde tutor de empresa",
                    e,
                    "No se pudo registrar la falta porque la base de datos no tiene la estructura esperada. Revisa la tabla de faltas y vuelve a intentarlo."
            );
        } catch (Exception e) {
            ControllerMessageUtil.addSafeErrorMessage(
                    redirectAttributes,
                    log,
                    "Error al registrar falta desde tutor de empresa",
                    e,
                    "No se pudo registrar la falta. Inténtalo de nuevo."
            );
        }

        return "redirect:/tutor-empresa/faltas";
    }

    @PostMapping("/faltas/{idFalta}/verificar")
    public String verificarJustificanteEmpresa(@PathVariable Long idFalta,
                                               @RequestParam(value = "comentarioVerificacion", required = false) String comentarioVerificacion,
                                               Principal principal,
                                               RedirectAttributes redirectAttributes) {
        try {
            TutorEmpresa tutor = getTutorEmpresaAutenticado(principal);

            FaltaAsistencia falta = faltaAsistenciaService.findById(idFalta)
                    .orElseThrow(() -> new RuntimeException(
                            "Falta no encontrada con id: " + idFalta));

            Alumno alumno = getAlumnoAsignado(falta.getAlumno().getId(), tutor);
            faltaAsistenciaService.verificarJustificanteEmpresa(idFalta, tutor, comentarioVerificacion);

            if (alumno.getTutorCentro() != null) {
                notificacionService.enviarSistema(
                        alumno.getTutorCentro(),
                        TipoNotificacionEnum.AVISO,
                        "Justificante verificado por empresa",
                        "El tutor de empresa ha verificado el justificante de la falta del "
                                + falta.getFechaFalta() + " de " + alumno.getNombreCompleto() + "."
                );
            }

            redirectAttributes.addFlashAttribute("successMsg",
                    "Justificante verificado por empresa correctamente.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        } catch (Exception e) {
            ControllerMessageUtil.addSafeErrorMessage(
                    redirectAttributes,
                    log,
                    "Error al verificar justificante desde tutor de empresa",
                    e,
                    "No se pudo verificar el justificante. Inténtalo de nuevo."
            );
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
            ControllerMessageUtil.addSafeErrorMessage(
                    redirectAttributes,
                    log,
                    "Error al emitir valoracion del tutor de empresa",
                    e,
                    "No se pudo emitir la valoración. Inténtalo de nuevo."
            );
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
            ControllerMessageUtil.addSafeErrorMessage(
                    redirectAttributes,
                    log,
                    "Error al bloquear valoracion del tutor de empresa",
                    e,
                    "No se pudo bloquear la valoración. Inténtalo de nuevo."
            );
        }

        return "redirect:/tutor-empresa/valoracion";
    }


    // HELPERS PRIVADOS

    private TutorEmpresa getTutorEmpresaAutenticado(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("No hay usuario autenticado");
        }

        return tutorEmpresaService.findByNombreUsuario(principal.getName())
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
        List<Asignacion> asignaciones = asignacionService.findActivasByTutorEmpresaId(tutor.getId());

        return asignaciones.stream()
                .map(Asignacion::getAlumno)
                .filter(alumno -> alumno.getId().equals(idAlumno))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "El alumno no está asignado a este tutor de empresa"));
    }

    private record DocumentacionAlumnoView(
            Long idAlumno,
            String nombreAlumno,
            String nombreUsuario,
            String grupo,
            String nombreEmpresa,
            List<Documento> documentos,
            List<Convenio> convenios
    ) {
    }

}
