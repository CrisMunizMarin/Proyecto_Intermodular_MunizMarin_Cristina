package com.mentorcore.controller;

import com.mentorcore.model.Alumno;
import com.mentorcore.model.Asignacion;
import com.mentorcore.model.Convenio;
import com.mentorcore.model.Documento;
import com.mentorcore.model.Notificacion;
import com.mentorcore.model.Tarea;
import com.mentorcore.model.TipoDocumento;
import com.mentorcore.model.TutorCentro;
import com.mentorcore.model.Valoracion;
import com.mentorcore.model.FaltaAsistencia;
import com.mentorcore.model.enums.ContextoDocumentoEnum;
import com.mentorcore.model.enums.TipoEvaluadorEnum;
import com.mentorcore.model.enums.ResultadoEnum;
import com.mentorcore.model.enums.EstadoFaltaEnum;
import com.mentorcore.service.AlumnoService;
import com.mentorcore.service.AsignacionService;
import com.mentorcore.service.ConvenioService;
import com.mentorcore.service.DocumentoService;
import com.mentorcore.service.EmpresaService;
import com.mentorcore.service.InformeService;
import com.mentorcore.service.NotificacionService;
import com.mentorcore.service.TareaService;
import com.mentorcore.service.TipoDocumentoService;
import com.mentorcore.service.TutorCentroService;
import com.mentorcore.service.ValoracionService;
import com.mentorcore.service.FaltaAsistenciaService;
import com.mentorcore.util.ControllerMessageUtil;
import com.mentorcore.util.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.math.BigDecimal;
import java.time.LocalDate;

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

    private static final Set<String> EXTENSIONES_PLAN_FORMACION = Set.of("pdf", "doc", "docx", "odt");

    private final AlumnoService alumnoService;
    private final TareaService tareaService;
    private final DocumentoService documentoService;
    private final ConvenioService convenioService;
    private final NotificacionService notificacionService;
    private final TutorCentroService tutorCentroService;
    private final ValoracionService valoracionService;
    private final InformeService informeService;
    private final EmpresaService empresaService;
    private final AsignacionService asignacionService;
    private final FaltaAsistenciaService faltaAsistenciaService;
    private final TipoDocumentoService tipoDocumentoService;
    private final FileUploadUtil fileUploadUtil;

    @Value("${mentorcore.uploads.ruta-base}")
    private String rutaBaseUploads;



    // INICIO

    @GetMapping("/inicio")
    public String inicio(Model model, Principal principal) {
        TutorCentro tutor = getTutorCentroAutenticado(principal);
        cargarDatosBase(model, tutor);

        List<Alumno> alumnos = alumnoService.findByTutorCentro(tutor);
        List<TareaPendienteView> tareasPendientes = construirTareasPendientesView(
                tareaService.findPendientesByTutorCentro(tutor.getId()));

        model.addAttribute("seccionActiva", "inicio");
        model.addAttribute("totalAlumnos", alumnos.size());
        model.addAttribute("tareasPendientes", tareasPendientes);
        model.addAttribute("notificacionesNoLeidas",
                notificacionService.contarNoLeidas(tutor));

        return "tutor-centro/inicio";
    }


    // ALUMNOS

    @GetMapping("/alumnos")
    public String alumnos(Model model, Principal principal) {
        TutorCentro tutor = getTutorCentroAutenticado(principal);
        cargarDatosBase(model, tutor);

        model.addAttribute("seccionActiva", "alumnos");
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
        List<Tarea> tareas = new ArrayList<>();
        List<Documento> documentos = new ArrayList<>();
        List<Documento> documentosPersonales = new ArrayList<>();
        List<Documento> documentosFe = new ArrayList<>();
        List<Convenio> convenios = new ArrayList<>();
        List<Valoracion> valoraciones = new ArrayList<>();

        try {
            tareas = tareaService.findByAlumno(alumno);
        } catch (Exception e) {
            log.warn("No se pudieron cargar las tareas del alumno id={} en tutor-centro detalle", id, e);
        }

        try {
            documentos = documentoService.findByAlumno(alumno);
            documentosPersonales = documentoService.findPersonalesByAlumno(alumno);
            documentosFe = documentoService.findFormacionEmpresaByAlumno(alumno);
        } catch (Exception e) {
            log.warn("No se pudieron cargar los documentos del alumno id={} en tutor-centro detalle", id, e);
        }

        try {
            convenios = convenioService.findByAlumno(alumno);
        } catch (Exception e) {
            log.warn("No se pudieron cargar los convenios del alumno id={} en tutor-centro detalle", id, e);
        }

        try {
            valoraciones = valoracionService.findByAlumno(alumno);
        } catch (Exception e) {
            log.warn("No se pudieron cargar las valoraciones del alumno id={} en tutor-centro detalle", id, e);
        }

        model.addAttribute("seccionActiva", "alumnos");
        model.addAttribute("alumno", alumno);
        model.addAttribute("tareas", tareas);
        model.addAttribute("documentos", documentos);
        model.addAttribute("documentosPersonales", documentosPersonales);
        model.addAttribute("documentosFe", documentosFe);
        model.addAttribute("convenios", convenios);
        model.addAttribute("valoraciones", valoraciones);
        model.addAttribute("asignacionActiva",
                asignacionService.findAsignacionActiva(alumno).orElse(null));

        return "tutor-centro/detalle-alumno";
    }

    @GetMapping("/busqueda-alumno")
    public String busquedaAlumno(Model model, Principal principal) {
        TutorCentro tutor = getTutorCentroAutenticado(principal);
        cargarDatosBase(model, tutor);

        model.addAttribute("seccionActiva", "alumnos");
        model.addAttribute("alumnos", alumnoService.findByTutorCentro(tutor));

        return "tutor-centro/busqueda-alumno";
    }


    // TAREAS

    @GetMapping("/tareas")
    public String tareas(Model model, Principal principal) {
        TutorCentro tutor = getTutorCentroAutenticado(principal);
        cargarDatosBase(model, tutor);

        model.addAttribute("seccionActiva", "tareas");
        model.addAttribute("tareasPendientes",
                construirTareasPendientesView(tareaService.findPendientesByTutorCentro(tutor.getId())));

        return "tutor-centro/tareas";
    }


    // DOCUMENTOS

    @GetMapping("/documentos")
    public String documentos(Model model, Principal principal) {
        TutorCentro tutor = getTutorCentroAutenticado(principal);
        cargarDatosBase(model, tutor);

        List<Alumno> alumnos = alumnoService.findByTutorCentro(tutor);
        List<Documento> documentosPendientes = new ArrayList<>();
        List<FaltaAsistencia> justificantesPendientes = new ArrayList<>();
        List<DocumentacionAlumnoView> documentacion = new ArrayList<>();

        for (Alumno alumno : alumnos) {
            documentosPendientes.addAll(documentoService.findPendientesByAlumno(alumno));
            documentacion.add(new DocumentacionAlumnoView(
                    alumno.getId(),
                    alumno.getNombreCompleto(),
                    alumno.getNombreUsuario(),
                    alumno.getGrupo(),
                    documentoService.findPersonalesByAlumno(alumno),
                    documentoService.findFormacionEmpresaByAlumno(alumno),
                    convenioService.findByAlumno(alumno)
            ));
        }

        documentosPendientes.removeIf(Documento::esJustificante);

        for (FaltaAsistencia falta : faltaAsistenciaService.findByTutorCentro(tutor.getId())) {
            if ((falta.getEstado() == EstadoFaltaEnum.PENDIENTE_REVISION
                    || falta.getEstado() == EstadoFaltaEnum.VERIFICADA_EMPRESA)
                    && falta.getJustificante() != null) {
                justificantesPendientes.add(falta);
            }
        }

        model.addAttribute("seccionActiva", "documentos");
        model.addAttribute("documentosPendientes", documentosPendientes);
        model.addAttribute("justificantesPendientes", justificantesPendientes);
        model.addAttribute("documentacion", documentacion);
        model.addAttribute("tiposDocumentoFeTutorCentro",
                tipoDocumentoService.findActivosPorNombres("Anexo I - Plan de Formación"));

        return "tutor-centro/documentos";
    }

    @PostMapping("/documentos/subir")
    public String subirDocumentoFeTutorCentro(@RequestParam("idAlumno") Long idAlumno,
                                              @RequestParam("idTipoDocumento") Long idTipoDocumento,
                                              @RequestParam("archivo") MultipartFile archivo,
                                              Principal principal,
                                              RedirectAttributes redirectAttributes) {
        try {
            TutorCentro tutor = getTutorCentroAutenticado(principal);
            Alumno alumno = getAlumnoDelTutor(idAlumno, tutor);

            TipoDocumento tipoDocumento = tipoDocumentoService.findById(idTipoDocumento)
                    .orElseThrow(() -> new RuntimeException(
                            "Tipo de documento no encontrado con id: " + idTipoDocumento));

            if (!"Anexo I - Plan de Formación".equals(tipoDocumento.getNombre())) {
                throw new IllegalArgumentException(
                        "Desde esta sección el tutor de centro solo puede subir el plan de formación");
            }

            if (archivo == null || archivo.isEmpty()) {
                throw new IllegalArgumentException("Debes seleccionar un archivo");
            }

            String extension = "";
            String nombreOriginal = archivo.getOriginalFilename();
            if (nombreOriginal != null && nombreOriginal.contains(".")) {
                extension = nombreOriginal.substring(nombreOriginal.lastIndexOf(".") + 1).toLowerCase();
            }

            boolean extensionValida = "Anexo I - Plan de Formación".equals(tipoDocumento.getNombre())
                    ? EXTENSIONES_PLAN_FORMACION.contains(extension)
                    : tipoDocumento.isExtensionValida(extension);

            if (!extensionValida) {
                throw new IllegalArgumentException(
                        "La extensión del archivo no está permitida para este tipo de documento");
            }

            String subcarpeta = "alumnos/" + alumno.getId() + "/formacion-empresa";
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
                    tipoDocumento.isEsObligatorio(),
                    ContextoDocumentoEnum.FORMACION_EMPRESA
            );

            redirectAttributes.addFlashAttribute("successMsg",
                    "Plan de formación subido correctamente.");
        } catch (Exception e) {
            ControllerMessageUtil.addSafeErrorMessage(
                    redirectAttributes,
                    log,
                    "Error al subir plan de formación desde tutor de centro",
                    e,
                    "No se pudo subir el documento FE. Inténtalo de nuevo."
            );
        }

        return "redirect:/tutor-centro/documentos";
    }

    @PostMapping("/documentos/convenio/{idConvenio}/subir")
    public String subirPdfConvenio(@PathVariable Long idConvenio,
                                   @RequestParam("archivo") MultipartFile archivo,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {
        try {
            TutorCentro tutor = getTutorCentroAutenticado(principal);
            Convenio convenio = convenioService.findById(idConvenio)
                    .orElseThrow(() -> new RuntimeException(
                            "Convenio no encontrado con id: " + idConvenio));

            Alumno alumno = getAlumnoDelTutor(convenio.getAlumno().getId(), tutor);

            if (archivo == null || archivo.isEmpty()) {
                throw new IllegalArgumentException("Debes seleccionar un PDF de convenio");
            }

            String subcarpeta = "alumnos/" + alumno.getId() + "/convenios";
            String rutaGuardada = fileUploadUtil.guardarArchivo(
                    rutaBaseUploads,
                    subcarpeta,
                    archivo
            );

            convenioService.guardarPdf(idConvenio, rutaGuardada);

            redirectAttributes.addFlashAttribute("successMsg",
                    "PDF del convenio actualizado correctamente.");
        } catch (Exception e) {
            ControllerMessageUtil.addSafeErrorMessage(
                    redirectAttributes,
                    log,
                    "Error al subir PDF de convenio desde tutor de centro",
                    e,
                    "No se pudo actualizar el PDF del convenio. Inténtalo de nuevo."
            );
        }

        return "redirect:/tutor-centro/documentos";
    }

    @PostMapping("/documentos/convenio/crear")
    public String crearConvenioInicial(@RequestParam("idAlumno") Long idAlumno,
                                       Principal principal,
                                       RedirectAttributes redirectAttributes) {
        try {
            TutorCentro tutor = getTutorCentroAutenticado(principal);
            Alumno alumno = getAlumnoDelTutor(idAlumno, tutor);
            Asignacion asignacion = asignacionService.findAsignacionActiva(alumno)
                    .orElseThrow(() -> new IllegalStateException(
                            "El alumno no tiene una asignación activa para generar el convenio"));

            asignacionService.asegurarConvenioInicial(asignacion);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Convenio inicial generado correctamente. Ya puedes subir el PDF.");
        } catch (Exception e) {
            ControllerMessageUtil.addSafeErrorMessage(
                    redirectAttributes,
                    log,
                    "Error al crear convenio inicial desde tutor de centro",
                    e,
                    "No se pudo generar el convenio inicial. Inténtalo de nuevo."
            );
        }

        return "redirect:/tutor-centro/documentos";
    }


    // NOTIFICACIONES

    @GetMapping("/notificaciones")
    public String notificaciones(Model model, Principal principal) {
        TutorCentro tutor = getTutorCentroAutenticado(principal);
        cargarDatosBase(model, tutor);

        List<Notificacion> notificaciones = notificacionService.findByReceptor(tutor);
        model.addAttribute("seccionActiva", "notificaciones");
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

        model.addAttribute("seccionActiva", "empresas");
        model.addAttribute("empresas", empresaService.findActivas());

        return "tutor-centro/empresas";
    }


    // ASIGNACIONES

    @GetMapping("/asignaciones")
    public String asignaciones(Model model, Principal principal) {
        TutorCentro tutor = getTutorCentroAutenticado(principal);
        cargarDatosBase(model, tutor);

        List<Alumno> alumnos = alumnoService.findByTutorCentro(tutor);
        model.addAttribute("seccionActiva", "asignaciones");
        model.addAttribute("alumnos", alumnos);

        return "tutor-centro/asignaciones";
    }


    // INFORMES

    @GetMapping("/informes")
    public String informes(Model model, Principal principal) {
        TutorCentro tutor = getTutorCentroAutenticado(principal);
        cargarDatosBase(model, tutor);

        model.addAttribute("seccionActiva", "informes");
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
        Map<Long, Valoracion> valoracionesEmpresaPorAlumno = new HashMap<>();

        for (Alumno alumno : alumnos) {
            valoracionService.findByAlumnoYTipo(alumno, TipoEvaluadorEnum.TUTOR_CENTRO)
                    .ifPresent(valoracionesTutorCentro::add);
            valoracionService.findByAlumnoYTipo(alumno, TipoEvaluadorEnum.TUTOR_EMPRESA)
                    .ifPresent(valoracion -> valoracionesEmpresaPorAlumno.put(alumno.getId(), valoracion));
        }

        model.addAttribute("seccionActiva", "valoracion");
        model.addAttribute("alumnos", alumnos);
        model.addAttribute("valoraciones", valoracionesTutorCentro);
        model.addAttribute("valoracionesEmpresaPorAlumno", valoracionesEmpresaPorAlumno);

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
            Tarea tarea = tareaService.findDetalleById(id)
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
            ControllerMessageUtil.addSafeErrorMessage(
                    redirectAttributes,
                    log,
                    "Error al validar tarea",
                    e,
                    "No se pudo validar la tarea. Inténtalo de nuevo."
            );
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
            Tarea tarea = tareaService.findDetalleById(id)
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
            ControllerMessageUtil.addSafeErrorMessage(
                    redirectAttributes,
                    log,
                    "Error al rechazar tarea",
                    e,
                    "No se pudo rechazar la tarea. Inténtalo de nuevo."
            );
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
            Tarea tarea = tareaService.findDetalleById(id)
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
            ControllerMessageUtil.addSafeErrorMessage(
                    redirectAttributes,
                    log,
                    "Error al devolver tarea para revision",
                    e,
                    "No se pudo revisar la tarea. Inténtalo de nuevo."
            );
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
            ControllerMessageUtil.addSafeErrorMessage(
                    redirectAttributes,
                    log,
                    "Error al validar documento",
                    e,
                    "No se pudo validar el documento. Inténtalo de nuevo."
            );
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
            ControllerMessageUtil.addSafeErrorMessage(
                    redirectAttributes,
                    log,
                    "Error al rechazar documento",
                    e,
                    "No se pudo rechazar el documento. Inténtalo de nuevo."
            );
        }

        return "redirect:/tutor-centro/documentos";
    }
    

    //EMISION DE VALORACION-ALUMNO
    @PostMapping("/valoracion/{idAlumno}/emitir")
    public String emitirValoracionTutorCentro(@PathVariable Long idAlumno,
                                              @RequestParam(value = "diarioPracticas", required = false) Integer diarioPracticas,
                                              @RequestParam(value = "puntualidadEntregas", required = false) Integer puntualidadEntregas,
                                              @RequestParam(value = "profundidadTecnica", required = false) Integer profundidadTecnica,
                                              @RequestParam(value = "relacionModulos", required = false) Integer relacionModulos,
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
                    diarioPracticas,
                    puntualidadEntregas,
                    profundidadTecnica,
                    relacionModulos,
                    observaciones,
                    resultado
            );

            notificacionService.enviarSistema(
                    alumno,
                    com.mentorcore.model.enums.TipoNotificacionEnum.AVISO,
                    "Nueva valoración del tutor de centro",
                    "Tu tutor de centro ha emitido o actualizado tu valoración final."
            );

            redirectAttributes.addFlashAttribute("successMsg",
                    "Valoración emitida correctamente.");
        } catch (Exception e) {
            ControllerMessageUtil.addSafeErrorMessage(
                    redirectAttributes,
                    log,
                    "Error al emitir valoracion del tutor de centro",
                    e,
                    "No se pudo emitir la valoración. Inténtalo de nuevo."
            );
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
            ControllerMessageUtil.addSafeErrorMessage(
                    redirectAttributes,
                    log,
                    "Error al bloquear valoracion del tutor de centro",
                    e,
                    "No se pudo bloquear la valoración. Inténtalo de nuevo."
            );
        }

        return "redirect:/tutor-centro/valoracion";
    }
    
    //APROBAR FALTA ASISTENCIA
    @PostMapping("/faltas/{idFalta}/aprobar")
    public String aprobarJustificante(@PathVariable Long idFalta,
                                      @RequestParam(value = "comentario", required = false) String comentario,
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

            faltaAsistenciaService.aprobarJustificante(idFalta, tutor, comentario);

            redirectAttributes.addFlashAttribute("successMsg",
                    "Justificante aprobado correctamente.");
        } catch (Exception e) {
            ControllerMessageUtil.addSafeErrorMessage(
                    redirectAttributes,
                    log,
                    "Error al aprobar justificante",
                    e,
                    "No se pudo aprobar el justificante. Inténtalo de nuevo."
            );
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
            ControllerMessageUtil.addSafeErrorMessage(
                    redirectAttributes,
                    log,
                    "Error al denegar justificante",
                    e,
                    "No se pudo denegar el justificante. Inténtalo de nuevo."
            );
        }

        return "redirect:/tutor-centro/documentos";
    }


    
    // HELPERS PRIVADOS

    private TutorCentro getTutorCentroAutenticado(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("No hay usuario autenticado");
        }

        return tutorCentroService.findByNombreUsuario(principal.getName())
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

    private List<TareaPendienteView> construirTareasPendientesView(List<Tarea> tareas) {
        List<TareaPendienteView> resultado = new ArrayList<>();

        for (Tarea tarea : tareas) {
            Alumno alumno = tarea.getAlumno();
            String nombreAlumno = alumno != null ? alumno.getNombreCompleto() : "Alumno";

            resultado.add(new TareaPendienteView(
                    tarea.getId(),
                    nombreAlumno,
                    tarea.getFechaRegistro(),
                    tarea.getHorasDedicadas(),
                    tarea.getAreaActividad(),
                    tarea.getDescripcion(),
                    tarea.getValoracionTutorEmpresa(),
                    tarea.getComentarioTutorEmpresa()
            ));
        }

        return resultado;
    }

    private record TareaPendienteView(
            Long id,
            String nombreAlumno,
            LocalDate fechaRegistro,
            BigDecimal horasDedicadas,
            String areaActividad,
            String descripcion,
            Integer valoracionTutorEmpresa,
            String comentarioTutorEmpresa
    ) {
    }

    private record DocumentacionAlumnoView(
            Long idAlumno,
            String nombreAlumno,
            String nombreUsuario,
            String grupo,
            List<Documento> documentosPersonales,
            List<Documento> documentosFe,
            List<Convenio> convenios
    ) {
    }
}
