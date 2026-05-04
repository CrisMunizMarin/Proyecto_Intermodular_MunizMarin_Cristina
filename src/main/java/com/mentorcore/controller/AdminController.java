package com.mentorcore.controller;

import com.mentorcore.model.Administrador;
import com.mentorcore.model.Alumno;
import com.mentorcore.model.CursoAcademico;
import com.mentorcore.model.Empresa;
import com.mentorcore.model.ParametroSistema;
import com.mentorcore.model.PeriodoFormacion;
import com.mentorcore.model.TutorCentro;
import com.mentorcore.model.TutorEmpresa;
import com.mentorcore.model.Usuario;
import com.mentorcore.model.enums.EstadoFeEnum;
import com.mentorcore.model.enums.NivelEnum;
import com.mentorcore.model.enums.RolEnum;
import com.mentorcore.service.AlumnoService;
import com.mentorcore.service.AsignacionService;
import com.mentorcore.service.CursoAcademicoService;
import com.mentorcore.service.EmpresaService;
import com.mentorcore.service.ParametroSistemaService;
import com.mentorcore.service.PeriodoFormacionService;
import com.mentorcore.service.TutorCentroService;
import com.mentorcore.service.TutorEmpresaService;
import com.mentorcore.service.UsuarioService;
import com.mentorcore.util.ControllerMessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

/**
 * Controlador del panel de administración.
 * Gestiona navegación, listados y acciones del administrador.
 * RF10, RF11, RF12, RF13, RF18, RF20, RF21
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final UsuarioService usuarioService;
    private final EmpresaService empresaService;
    private final CursoAcademicoService cursoAcademicoService;
    private final ParametroSistemaService parametroSistemaService;
    private final AlumnoService alumnoService;
    private final TutorCentroService tutorCentroService;
    private final TutorEmpresaService tutorEmpresaService;
    private final AsignacionService asignacionService;
    private final PeriodoFormacionService periodoFormacionService;
    private final PasswordEncoder passwordEncoder;


    // PANEL PRINCIPAL

    @GetMapping("/inicio")
    public String inicio(Model model, Principal principal) {
        cargarAdminActual(model, principal);

        model.addAttribute("totalUsuarios", usuarioService.findAll().size());
        model.addAttribute("totalEmpresas", empresaService.findAll().size());
        model.addAttribute("totalCursos", cursoAcademicoService.findAll().size());
        model.addAttribute("totalAlumnos", alumnoService.findAll().size());
        model.addAttribute("totalPeriodos", periodoFormacionService.findAll().size());
        model.addAttribute("totalAsignacionesActivas",
                asignacionService.findAll().stream()
                        .filter(asignacion -> asignacion.getEstado() == EstadoFeEnum.EN_CURSO)
                        .count());

        return "admin/inicio";
    }


    // USUARIOS

    @GetMapping("/usuarios")
    public String usuarios(Model model, Principal principal) {
        cargarAdminActual(model, principal);

        model.addAttribute("usuarios", usuarioService.findAll());
        model.addAttribute("roles", RolEnum.values());

        return "admin/usuarios";
    }

    @GetMapping("/usuarios/crear")
    public String mostrarCrearUsuario(Model model, Principal principal) {
        cargarAdminActual(model, principal);
        cargarDatosAuxiliaresUsuario(model);
        model.addAttribute("roles", RolEnum.values());

        return "admin/crear-usuario";
    }

    @PostMapping("/usuarios/crear")
    public String crearUsuario(@RequestParam("rol") RolEnum rol,
                               @RequestParam("nombreUsuario") String nombreUsuario,
                               @RequestParam("email") String email,
                               @RequestParam("password") String password,
                               @RequestParam("nombre") String nombre,
                               @RequestParam("apellidos") String apellidos,
                               @RequestParam(value = "telefono", required = false) String telefono,
                               @RequestParam(value = "idCurso", required = false) Long idCurso,
                               @RequestParam(value = "idTutorCentro", required = false) Long idTutorCentro,
                               @RequestParam(value = "grupo", required = false) String grupo,
                               @RequestParam(value = "dni", required = false) String dni,
                               @RequestParam(value = "departamento", required = false) String departamento,
                               @RequestParam(value = "especialidad", required = false) String especialidad,
                               @RequestParam(value = "numExpedienteDocente", required = false) String numExpedienteDocente,
                               @RequestParam(value = "idEmpresa", required = false) Long idEmpresa,
                               @RequestParam(value = "cargo", required = false) String cargo,
                               @RequestParam(value = "departamentoEmpresa", required = false) String departamentoEmpresa,
                               RedirectAttributes redirectAttributes) {
        try {
            if (password == null || password.isBlank()) {
                throw new IllegalArgumentException("La contraseña es obligatoria");
            }

            Usuario usuario = construirUsuario(
                    rol,
                    nombreUsuario,
                    email,
                    nombre,
                    apellidos,
                    telefono,
                    idCurso,
                    idTutorCentro,
                    grupo,
                    dni,
                    departamento,
                    especialidad,
                    numExpedienteDocente,
                    idEmpresa,
                    cargo,
                    departamentoEmpresa
            );

            usuario.setPasswordHash(passwordEncoder.encode(password));
            usuarioService.actualizar(usuario);

            redirectAttributes.addFlashAttribute("successMsg",
                    "Usuario creado correctamente.");
            return "redirect:/admin/usuarios";
        } catch (Exception e) {
            ControllerMessageUtil.addSafeErrorMessage(
                    redirectAttributes,
                    log,
                    "Error al crear usuario",
                    e,
                    "No se pudo crear el usuario. Revisa los datos e inténtalo de nuevo."
            );
            return "redirect:/admin/usuarios/crear";
        }
    }

    @GetMapping("/usuarios/editar/{id}")
    public String mostrarEditarUsuario(@PathVariable Long id,
                                       Model model,
                                       Principal principal) {
        cargarAdminActual(model, principal);
        cargarDatosAuxiliaresUsuario(model);

        Usuario usuario = cargarUsuarioDetalle(id);

        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", RolEnum.values());
        model.addAttribute("cursoSeleccionadoId",
                usuario instanceof Alumno alumno && alumno.getCursoAcademico() != null
                        ? alumno.getCursoAcademico().getId()
                        : null);
        model.addAttribute("tutorCentroSeleccionadoId",
                usuario instanceof Alumno alumno && alumno.getTutorCentro() != null
                        ? alumno.getTutorCentro().getId()
                        : null);
        model.addAttribute("empresaSeleccionadaId",
                usuario instanceof TutorEmpresa tutorEmpresa && tutorEmpresa.getEmpresa() != null
                        ? tutorEmpresa.getEmpresa().getId()
                        : null);

        return "admin/editar-usuario";
    }

    @PostMapping("/usuarios/editar/{id}")
    public String editarUsuario(@PathVariable Long id,
                                @RequestParam("rol") RolEnum rol,
                                @RequestParam("nombreUsuario") String nombreUsuario,
                                @RequestParam("email") String email,
                                @RequestParam(value = "password", required = false) String password,
                                @RequestParam("nombre") String nombre,
                                @RequestParam("apellidos") String apellidos,
                                @RequestParam(value = "telefono", required = false) String telefono,
                                @RequestParam(value = "idCurso", required = false) Long idCurso,
                                @RequestParam(value = "idTutorCentro", required = false) Long idTutorCentro,
                                @RequestParam(value = "grupo", required = false) String grupo,
                                @RequestParam(value = "dni", required = false) String dni,
                                @RequestParam(value = "departamento", required = false) String departamento,
                                @RequestParam(value = "especialidad", required = false) String especialidad,
                                @RequestParam(value = "numExpedienteDocente", required = false) String numExpedienteDocente,
                                @RequestParam(value = "idEmpresa", required = false) Long idEmpresa,
                                @RequestParam(value = "cargo", required = false) String cargo,
                                @RequestParam(value = "departamentoEmpresa", required = false) String departamentoEmpresa,
                                RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = cargarUsuarioDetalle(id);

            if (usuario.getRol() != rol) {
                throw new IllegalArgumentException(
                        "Cambiar el rol de un usuario existente no está permitido desde esta pantalla.");
            }

            configurarBaseUsuario(usuario, nombreUsuario, email, nombre, apellidos, telefono);

            switch (rol) {
                case ALUMNO -> actualizarAlumno((Alumno) usuario, idCurso, idTutorCentro, grupo, dni);
                case TUTOR_CENTRO -> actualizarTutorCentro((TutorCentro) usuario, departamento, especialidad, numExpedienteDocente);
                case TUTOR_EMPRESA -> actualizarTutorEmpresa((TutorEmpresa) usuario, idEmpresa, cargo, departamentoEmpresa);
                case ADMIN -> {
                    // No requiere datos adicionales.
                }
            }

            if (password != null && !password.isBlank()) {
                usuario.setPasswordHash(passwordEncoder.encode(password));
            }

            usuarioService.actualizar(usuario);

            redirectAttributes.addFlashAttribute("successMsg",
                    "Usuario actualizado correctamente.");
        } catch (Exception e) {
            ControllerMessageUtil.addSafeErrorMessage(
                    redirectAttributes,
                    log,
                    "Error al editar usuario",
                    e,
                    "No se pudo actualizar el usuario. Inténtalo de nuevo."
            );
            return "redirect:/admin/usuarios/editar/" + id;
        }

        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuarios/suspender/{id}")
    public String suspenderUsuario(@PathVariable Long id,
                                   RedirectAttributes redirectAttributes) {
        usuarioService.suspender(id);
        redirectAttributes.addFlashAttribute("successMsg",
                "Usuario suspendido correctamente.");
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuarios/activar/{id}")
    public String activarUsuario(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        usuarioService.activar(id);
        redirectAttributes.addFlashAttribute("successMsg",
                "Usuario activado correctamente.");
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuarios/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id,
                                  RedirectAttributes redirectAttributes) {
        usuarioService.eliminar(id);
        redirectAttributes.addFlashAttribute("successMsg",
                "Usuario eliminado correctamente.");
        return "redirect:/admin/usuarios";
    }


    // EMPRESAS

    @GetMapping("/empresas")
    public String empresas(Model model, Principal principal) {
        cargarAdminActual(model, principal);
        model.addAttribute("empresas", empresaService.findAll());
        return "admin/empresas";
    }

    @GetMapping("/empresas/crear")
    public String mostrarCrearEmpresa(Model model, Principal principal) {
        cargarAdminActual(model, principal);
        model.addAttribute("empresa", new Empresa());
        return "admin/crear-empresa";
    }

    @PostMapping("/empresas/crear")
    public String crearEmpresa(@ModelAttribute Empresa empresa,
                               RedirectAttributes redirectAttributes) {
        try {
            empresaService.crear(empresa);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Empresa creada correctamente.");
            return "redirect:/admin/empresas";
        } catch (Exception e) {
            ControllerMessageUtil.addSafeErrorMessage(
                    redirectAttributes,
                    log,
                    "Error al crear empresa",
                    e,
                    "No se pudo crear la empresa. Inténtalo de nuevo."
            );
            return "redirect:/admin/empresas/crear";
        }
    }

    @GetMapping("/empresas/editar/{id}")
    public String mostrarEditarEmpresa(@PathVariable Long id,
                                       Model model,
                                       Principal principal) {
        cargarAdminActual(model, principal);

        Empresa empresa = empresaService.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Empresa no encontrada con id: " + id));

        model.addAttribute("empresa", empresa);
        return "admin/editar-empresa";
    }

    @PostMapping("/empresas/editar/{id}")
    public String editarEmpresa(@PathVariable Long id,
                                @ModelAttribute Empresa empresa,
                                RedirectAttributes redirectAttributes) {
        try {
            empresa.setId(id);
            empresaService.actualizar(empresa);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Empresa actualizada correctamente.");
            return "redirect:/admin/empresas";
        } catch (Exception e) {
            ControllerMessageUtil.addSafeErrorMessage(
                    redirectAttributes,
                    log,
                    "Error al editar empresa",
                    e,
                    "No se pudo actualizar la empresa. Inténtalo de nuevo."
            );
            return "redirect:/admin/empresas/editar/" + id;
        }
    }

    @PostMapping("/empresas/desactivar/{id}")
    public String desactivarEmpresa(@PathVariable Long id,
                                    RedirectAttributes redirectAttributes) {
        empresaService.desactivar(id);
        redirectAttributes.addFlashAttribute("successMsg",
                "Empresa desactivada correctamente.");
        return "redirect:/admin/empresas";
    }

    @PostMapping("/empresas/reactivar/{id}")
    public String reactivarEmpresa(@PathVariable Long id,
                                   RedirectAttributes redirectAttributes) {
        empresaService.reactivar(id);
        redirectAttributes.addFlashAttribute("successMsg",
                "Empresa reactivada correctamente.");
        return "redirect:/admin/empresas";
    }

    @PostMapping("/empresas/eliminar/{id}")
    public String eliminarEmpresa(@PathVariable Long id,
                                  RedirectAttributes redirectAttributes) {
        empresaService.eliminar(id);
        redirectAttributes.addFlashAttribute("successMsg",
                "Empresa eliminada correctamente.");
        return "redirect:/admin/empresas";
    }


    // CURSOS

    @GetMapping("/cursos")
    public String cursos(Model model, Principal principal) {
        cargarAdminActual(model, principal);

        model.addAttribute("cursos", cursoAcademicoService.findAll());
        model.addAttribute("niveles", NivelEnum.values());

        return "admin/cursos";
    }

    @PostMapping("/cursos/crear")
    public String crearCurso(@RequestParam("codigoCurso") String codigoCurso,
                             @RequestParam("nombre") String nombre,
                             @RequestParam("cicloFormativo") String cicloFormativo,
                             @RequestParam("nivel") NivelEnum nivel,
                             @RequestParam("anioAcademico") String anioAcademico,
                             RedirectAttributes redirectAttributes) {
        try {
            CursoAcademico curso = new CursoAcademico();
            curso.setCodigoCurso(normalizar(codigoCurso));
            curso.setNombre(normalizar(nombre));
            curso.setCicloFormativo(normalizar(cicloFormativo));
            curso.setNivel(nivel);
            curso.setAnioAcademico(normalizar(anioAcademico));

            cursoAcademicoService.crear(curso);

            redirectAttributes.addFlashAttribute("successMsg",
                    "Curso creado correctamente.");
        } catch (Exception e) {
            ControllerMessageUtil.addSafeErrorMessage(
                    redirectAttributes,
                    log,
                    "Error al crear curso",
                    e,
                    "No se pudo crear el curso. Revisa los datos e inténtalo de nuevo."
            );
        }

        return "redirect:/admin/cursos";
    }

    @PostMapping("/cursos/editar/{id}")
    public String editarCurso(@PathVariable Long id,
                              @RequestParam("codigoCurso") String codigoCurso,
                              @RequestParam("nombre") String nombre,
                              @RequestParam("cicloFormativo") String cicloFormativo,
                              @RequestParam("nivel") NivelEnum nivel,
                              @RequestParam("anioAcademico") String anioAcademico,
                              RedirectAttributes redirectAttributes) {
        try {
            CursoAcademico curso = cursoAcademicoService.findById(id)
                    .orElseThrow(() -> new RuntimeException(
                            "Curso no encontrado con id: " + id));

            curso.setCodigoCurso(normalizar(codigoCurso));
            curso.setNombre(normalizar(nombre));
            curso.setCicloFormativo(normalizar(cicloFormativo));
            curso.setNivel(nivel);
            curso.setAnioAcademico(normalizar(anioAcademico));

            cursoAcademicoService.actualizar(curso);

            redirectAttributes.addFlashAttribute("successMsg",
                    "Curso actualizado correctamente.");
        } catch (Exception e) {
            ControllerMessageUtil.addSafeErrorMessage(
                    redirectAttributes,
                    log,
                    "Error al actualizar curso",
                    e,
                    "No se pudo actualizar el curso. Inténtalo de nuevo."
            );
        }

        return "redirect:/admin/cursos";
    }

    @PostMapping("/cursos/activar/{id}")
    public String activarCurso(@PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
        cursoAcademicoService.activar(id);
        redirectAttributes.addFlashAttribute("successMsg",
                "Curso activado correctamente.");
        return "redirect:/admin/cursos";
    }

    @PostMapping("/cursos/desactivar/{id}")
    public String desactivarCurso(@PathVariable Long id,
                                  RedirectAttributes redirectAttributes) {
        cursoAcademicoService.desactivar(id);
        redirectAttributes.addFlashAttribute("successMsg",
                "Curso desactivado correctamente.");
        return "redirect:/admin/cursos";
    }

    @PostMapping("/cursos/eliminar/{id}")
    public String eliminarCurso(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        cursoAcademicoService.eliminar(id);
        redirectAttributes.addFlashAttribute("successMsg",
                "Curso eliminado correctamente.");
        return "redirect:/admin/cursos";
    }


    // CONFIGURACIÓN

    @GetMapping("/configuracion")
    public String configuracion(Model model, Principal principal) {
        cargarAdminActual(model, principal);
        model.addAttribute("parametros", parametroSistemaService.findAll());
        return "admin/configuracion";
    }

    @PostMapping("/configuracion/actualizar/{id}")
    public String actualizarParametro(@PathVariable Long id,
                                      @RequestParam("valor") String valor,
                                      Principal principal,
                                      RedirectAttributes redirectAttributes) {
        try {
            Usuario admin = usuarioService.findByNombreUsuario(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Administrador no autenticado"));

            ParametroSistema parametro = parametroSistemaService.findById(id)
                    .orElseThrow(() -> new RuntimeException(
                            "Parámetro no encontrado con id: " + id));

            parametroSistemaService.actualizarValor(parametro.getClave(), valor, admin);

            redirectAttributes.addFlashAttribute("successMsg",
                    "Parámetro actualizado correctamente.");
        } catch (Exception e) {
            ControllerMessageUtil.addSafeErrorMessage(
                    redirectAttributes,
                    log,
                    "Error al actualizar parametro del sistema",
                    e,
                    "No se pudo actualizar la configuración. Inténtalo de nuevo."
            );
        }

        return "redirect:/admin/configuracion";
    }


    // HELPERS PRIVADOS

    private void cargarAdminActual(Model model, Principal principal) {
        if (principal == null) {
            return;
        }

        usuarioService.findByNombreUsuario(principal.getName())
                .ifPresent(admin -> model.addAttribute("adminActual", admin));
    }

    private void cargarDatosAuxiliaresUsuario(Model model) {
        model.addAttribute("cursos", cursoAcademicoService.findActivos());
        model.addAttribute("tutoresCentro", tutorCentroService.findActivos());
        model.addAttribute("empresasActivas", empresaService.findActivas());
    }

    private Usuario cargarUsuarioDetalle(Long id) {
        Usuario usuarioBase = usuarioService.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Usuario no encontrado con id: " + id));

        return switch (usuarioBase.getRol()) {
            case ALUMNO -> alumnoService.findById(id)
                    .orElseThrow(() -> new RuntimeException(
                            "Alumno no encontrado con id: " + id));
            case TUTOR_CENTRO -> tutorCentroService.findById(id)
                    .orElseThrow(() -> new RuntimeException(
                            "Tutor de centro no encontrado con id: " + id));
            case TUTOR_EMPRESA -> tutorEmpresaService.findById(id)
                    .orElseThrow(() -> new RuntimeException(
                            "Tutor de empresa no encontrado con id: " + id));
            case ADMIN -> usuarioBase;
        };
    }

    private Usuario construirUsuario(RolEnum rol,
                                     String nombreUsuario,
                                     String email,
                                     String nombre,
                                     String apellidos,
                                     String telefono,
                                     Long idCurso,
                                     Long idTutorCentro,
                                     String grupo,
                                     String dni,
                                     String departamento,
                                     String especialidad,
                                     String numExpedienteDocente,
                                     Long idEmpresa,
                                     String cargo,
                                     String departamentoEmpresa) {
        Usuario usuario = switch (rol) {
            case ALUMNO -> {
                CursoAcademico curso = cursoAcademicoService.findById(idCurso)
                        .orElseThrow(() -> new RuntimeException("Debes seleccionar un curso"));
                TutorCentro tutorCentro = tutorCentroService.findById(idTutorCentro)
                        .orElseThrow(() -> new RuntimeException("Debes seleccionar un tutor de centro"));

                Alumno alumno = new Alumno();
                alumno.setCursoAcademico(curso);
                alumno.setTutorCentro(tutorCentro);
                alumno.setGrupo(normalizarOpcional(grupo));
                alumno.setDni(normalizarOpcional(dni));
                yield alumno;
            }
            case TUTOR_CENTRO -> {
                TutorCentro tutorCentro = new TutorCentro();
                tutorCentro.setDepartamento(normalizarOpcional(departamento));
                tutorCentro.setEspecialidad(normalizarOpcional(especialidad));
                tutorCentro.setNumExpedienteDocente(normalizarOpcional(numExpedienteDocente));
                yield tutorCentro;
            }
            case TUTOR_EMPRESA -> {
                Empresa empresa = empresaService.findById(idEmpresa)
                        .orElseThrow(() -> new RuntimeException("Debes seleccionar una empresa"));

                TutorEmpresa tutorEmpresa = new TutorEmpresa();
                tutorEmpresa.setEmpresa(empresa);
                tutorEmpresa.setCargo(normalizarOpcional(cargo));
                tutorEmpresa.setDepartamentoEmpresa(normalizarOpcional(departamentoEmpresa));
                yield tutorEmpresa;
            }
            case ADMIN -> new Administrador();
        };

        usuario.setRol(rol);
        configurarBaseUsuario(usuario, nombreUsuario, email, nombre, apellidos, telefono);
        return usuario;
    }

    private void configurarBaseUsuario(Usuario usuario,
                                       String nombreUsuario,
                                       String email,
                                       String nombre,
                                       String apellidos,
                                       String telefono) {
        usuario.setNombreUsuario(normalizar(nombreUsuario));
        usuario.setEmail(normalizar(email));
        usuario.setNombre(normalizar(nombre));
        usuario.setApellidos(normalizar(apellidos));
        usuario.setTelefono(normalizarOpcional(telefono));
    }

    private void actualizarAlumno(Alumno alumno,
                                  Long idCurso,
                                  Long idTutorCentro,
                                  String grupo,
                                  String dni) {
        CursoAcademico curso = cursoAcademicoService.findById(idCurso)
                .orElseThrow(() -> new RuntimeException("Debes seleccionar un curso"));
        TutorCentro tutorCentro = tutorCentroService.findById(idTutorCentro)
                .orElseThrow(() -> new RuntimeException("Debes seleccionar un tutor de centro"));

        alumno.setCursoAcademico(curso);
        alumno.setTutorCentro(tutorCentro);
        alumno.setGrupo(normalizarOpcional(grupo));
        alumno.setDni(normalizarOpcional(dni));
    }

    private void actualizarTutorCentro(TutorCentro tutorCentro,
                                       String departamento,
                                       String especialidad,
                                       String numExpedienteDocente) {
        tutorCentro.setDepartamento(normalizarOpcional(departamento));
        tutorCentro.setEspecialidad(normalizarOpcional(especialidad));
        tutorCentro.setNumExpedienteDocente(normalizarOpcional(numExpedienteDocente));
    }

    private void actualizarTutorEmpresa(TutorEmpresa tutorEmpresa,
                                        Long idEmpresa,
                                        String cargo,
                                        String departamentoEmpresa) {
        Empresa empresa = empresaService.findById(idEmpresa)
                .orElseThrow(() -> new RuntimeException("Debes seleccionar una empresa"));

        tutorEmpresa.setEmpresa(empresa);
        tutorEmpresa.setCargo(normalizarOpcional(cargo));
        tutorEmpresa.setDepartamentoEmpresa(normalizarOpcional(departamentoEmpresa));
    }

    private String normalizar(String texto) {
        if (texto == null || texto.isBlank()) {
            return "";
        }
        return texto.trim();
    }

    private String normalizarOpcional(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }
}
