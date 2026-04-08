package com.mentorcore.controller;

import com.mentorcore.model.CursoAcademico;
import com.mentorcore.model.Empresa;
import com.mentorcore.model.ParametroSistema;
import com.mentorcore.model.Usuario;
import com.mentorcore.model.enums.RolEnum;
import com.mentorcore.service.AlumnoService;
import com.mentorcore.service.CursoAcademicoService;
import com.mentorcore.service.EmpresaService;
import com.mentorcore.service.ParametroSistemaService;
import com.mentorcore.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

/**
 * Controlador del panel de administración.
 * Gestiona navegación, listados y acciones base del administrador.
 * RF10, RF11, RF12, RF13, RF18, RF20
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


    // PANEL PRINCIPAL

    @GetMapping("/inicio")
    public String inicio(Model model, Principal principal) {
        cargarAdminActual(model, principal);

        model.addAttribute("totalUsuarios", usuarioService.findAll().size());
        model.addAttribute("totalEmpresas", empresaService.findAll().size());
        model.addAttribute("totalCursos", cursoAcademicoService.findAll().size());
        model.addAttribute("totalAlumnos", alumnoService.findAll().size());

        return "admin/inicio";
    }


    // USUARIOS

    @GetMapping("/usuarios")
    public String usuarios(Model model, Principal principal) {
        cargarAdminActual(model, principal);

        List<Usuario> usuarios = usuarioService.findAll();
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("roles", RolEnum.values());

        return "admin/usuarios";
    }

    @GetMapping("/usuarios/crear")
    public String mostrarCrearUsuario(Model model, Principal principal) {
        cargarAdminActual(model, principal);

        model.addAttribute("roles", RolEnum.values());

        return "admin/crear-usuario";
    }

    @GetMapping("/usuarios/editar/{id}")
    public String mostrarEditarUsuario(@PathVariable Long id,
                                       Model model,
                                       Principal principal) {
        cargarAdminActual(model, principal);

        Usuario usuario = usuarioService.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Usuario no encontrado con id: " + id));

        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", RolEnum.values());

        return "admin/editar-usuario";
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

        List<Empresa> empresas = empresaService.findAll();
        model.addAttribute("empresas", empresas);

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
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
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
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
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

        List<CursoAcademico> cursos = cursoAcademicoService.findAll();
        model.addAttribute("cursos", cursos);

        return "admin/cursos";
    }


    // CONFIGURACIÓN

    @GetMapping("/configuracion")
    public String configuracion(Model model, Principal principal) {
        cargarAdminActual(model, principal);

        List<ParametroSistema> parametros = parametroSistemaService.findAll();
        model.addAttribute("parametros", parametros);

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
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/admin/configuracion";
    }


    // VISTAS PREPARADAS PARA SIGUIENTE FASE

    @GetMapping("/periodos")
    public String periodos(Model model, Principal principal) {
        cargarAdminActual(model, principal);
        return "admin/periodos";
    }

    @GetMapping("/asignaciones")
    public String asignaciones(Model model, Principal principal) {
        cargarAdminActual(model, principal);
        return "admin/asignaciones";
    }


    // HELPERS PRIVADOS

    private void cargarAdminActual(Model model, Principal principal) {
        if (principal == null) {
            return;
        }

        usuarioService.findByNombreUsuario(principal.getName())
                .ifPresent(admin -> model.addAttribute("adminActual", admin));
    }
}


