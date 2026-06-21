package com.proyecto.AccesoUsuarios.controller;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.lowagie.text.DocumentException;
import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.services.UsuarioService;
import com.proyecto.AccesoUsuarios.Utils.UsuarioExporterPDF;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;


    public UsuarioController(UsuarioService usuarioService, PasswordEncoder passwordEncoder) {
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder;
    }

    // ---------------------------------------------------------
    // 1. NAVEGACIÓN PRINCIPAL
    // ---------------------------------------------------------

    @GetMapping("/")
    public String index() {
        return "index"; 
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    // ---------------------------------------------------------
    // 2. REGISTRO Y GESTIÓN
    // ---------------------------------------------------------

    // 1. ESTO SOLO MUESTRA EL FORMULARIO VACÍO
    @GetMapping("/registro")
    public String registroForm(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "form"; // El nombre de tu archivo HTML
    }

    // 2. ESTO RECIBE LOS DATOS CUANDO EL USUARIO DA CLIC EN "REGISTRARSE"
    @PostMapping("/registro")
    public String registrarUsuario(@ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes) {
        
        // Validar si el nombre de usuario ya existe
        if (usuarioService.existsByUserName(usuario.getUserName())) {
            redirectAttributes.addFlashAttribute("error", "El nombre de usuario '" + usuario.getUserName() + "' ya existe.");
            return "redirect:/registro"; 
        }

        try {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            usuario.setHabilitado(true);
            usuario.setNombre(usuario.getNombre() != null ? usuario.getNombre() : usuario.getUserName());
            usuario.setApellido(usuario.getApellido() != null ? usuario.getApellido() : "");
            usuario.setDocumento(usuario.getDocumento() != null ? usuario.getDocumento() : "-");
            usuarioService.save(usuario);
            redirectAttributes.addFlashAttribute("exito", "¡Usuario registrado correctamente!");
            return "redirect:/login"; 
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al registrar: " + e.getMessage());
            return "redirect:/registro";
        }
    }

    @GetMapping("/registro/estudiante")
    public String registroEstudianteForm(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro-estudiante";
    }

    @PostMapping("/registro/estudiante")
    public String registrarEstudiante(@ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes) {
        if (usuarioService.existsByUserName(usuario.getUserName())) {
            redirectAttributes.addFlashAttribute("error", "El nombre de usuario ya existe.");
            return "redirect:/registro/estudiante";
        }
        if (usuario.getEmail() != null && usuarioService.existsByEmail(usuario.getEmail())) {
            redirectAttributes.addFlashAttribute("error", "El correo ya esta registrado.");
            return "redirect:/registro/estudiante";
        }
        try {
            usuario.setRol("USER");
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            usuario.setHabilitado(true);
            usuario.setNombre(usuario.getNombre() != null ? usuario.getNombre() : usuario.getUserName());
            usuario.setApellido(usuario.getApellido() != null ? usuario.getApellido() : "");
            usuario.setDocumento(usuario.getDocumento() != null ? usuario.getDocumento() : "-");
            usuarioService.save(usuario);
            redirectAttributes.addFlashAttribute("exito", "Estudiante registrado correctamente!");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/registro/estudiante";
        }
    }

    @GetMapping("/registro/institucion")
    public String registroInstitucionForm(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro-institucion";
    }

    @GetMapping("/registro/institucion/espera")
    public String esperaInstitucion() {
        return "espera-institucion";
    }

    @GetMapping("/registro/institucion/rechazo")
    public String rechazoInstitucion() {
        return "rechazo-institucion";
    }

    @PostMapping("/registro/institucion")
    public String registrarInstitucion(@ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes) {
        if (usuarioService.existsByUserName(usuario.getUserName())) {
            redirectAttributes.addFlashAttribute("error", "El nombre de usuario ya existe.");
            return "redirect:/registro/institucion";
        }
        if (usuario.getEmail() != null && usuarioService.existsByEmail(usuario.getEmail())) {
            redirectAttributes.addFlashAttribute("error", "El correo ya esta registrado.");
            return "redirect:/registro/institucion";
        }
        try {
            usuario.setRol("INSTITUCION");
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            usuario.setHabilitado(true);
            usuario.setEstadoCuenta("PENDIENTE");
            usuario.setNombre(usuario.getNombre() != null ? usuario.getNombre() : usuario.getNombreInstitucion() != null ? usuario.getNombreInstitucion() : "Institucion");
            usuario.setApellido(usuario.getApellido() != null ? usuario.getApellido() : "");
            usuario.setDocumento(usuario.getDocumento() != null ? usuario.getDocumento() : usuario.getNit() != null ? usuario.getNit() : "-");
            usuarioService.save(usuario);
            redirectAttributes.addFlashAttribute("nombreInstitucion", usuario.getNombreInstitucion());
            return "redirect:/registro/institucion/espera";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/registro/institucion";
        }
    }

    // --- NUEVO MÉTODO PARA EL BOTÓN "NUEVO USUARIO" DEL ADMIN ---
    @GetMapping("/usuarios/nuevo")
    public String crearUsuarioAdmin(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "form"; // Reutilizamos el formulario "form.html"
    }
    // ------------------------------------------------------------

    @PostMapping("/guardarUsuario")
    public String guardarUsuario(@ModelAttribute Usuario usuario, Authentication auth) {
        
        // LOGICA DE CONTRASEÑA
        if (usuario.getId() != null) {
            Usuario usuarioExistente = usuarioService.obtenerPorId(usuario.getId());
            if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
                usuario.setPassword(usuarioExistente.getPassword());
            } else {
                usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            }
        } else {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }

        usuario.setHabilitado(true); 
        
        // SEGURIDAD DE ROLES
        if ("ADMIN".equals(usuario.getRol())) {
            if (auth == null || !auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                usuario.setRol("USER");
            }
        }

        if (usuario.getRol() == null || usuario.getRol().isEmpty()) {
             usuario.setRol("USER");
        }

        usuarioService.save(usuario);
        
        // Redirección inteligente
        if (auth != null && auth.isAuthenticated()) {
            return "redirect:/usuarios";
        }
        
        return "redirect:/login?registrado";
    }

    // ---------------------------------------------------------
    // 3. GESTIÓN DE USUARIOS (ADMIN) - CON FILTROS
    // ---------------------------------------------------------

    @GetMapping("/usuarios")
    public String listarUsuarios(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "rol", required = false) String rol,
            Model model) {
        
        // Limpiamos los valores para que la Query funcione correctamente
        String k = (keyword != null && !keyword.trim().isEmpty()) ? keyword : null;
        String r = (rol != null && !rol.trim().isEmpty() && !rol.equals("Todos")) ? rol : null;

        List<Usuario> listaUsuarios = usuarioService.filtrarUsuarios(k, r);
        
        model.addAttribute("usuarios", listaUsuarios);
        model.addAttribute("keyword", keyword);
        model.addAttribute("rol", rol);
        
        return "usuarios";
    }

    @GetMapping("/usuarios/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable("id") Long id, Model model) {
        Usuario usuario = usuarioService.obtenerPorId(id);
        // Limpiar password para no mostrar hash en la vista
        usuario.setPassword(""); 
        model.addAttribute("usuario", usuario);
        return "editar"; 
    }

    // 2. Procesar la actualización
    @PostMapping("/usuarios/actualizar")
    public String actualizarUsuario(@ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.save(usuario); // Como el objeto ya trae el ID, JPA hará un UPDATE
            redirectAttributes.addFlashAttribute("exito", "Usuario actualizado correctamente");
            return "redirect:/usuarios"; 
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar: " + e.getMessage());
            return "redirect:/usuarios/editar/" + usuario.getId();
        }
    }
    
    @GetMapping("/usuarios/eliminar/{id}")
    public String eliminarUsuario(@PathVariable("id") Long id) {
        // En lugar de borrar, cambiamos el estado a false
        Usuario u = usuarioService.obtenerPorId(id);
        u.setHabilitado(false);
        usuarioService.save(u);
        return "redirect:/usuarios";
    }

    // ESTE ES EL MÉTODO QUE CONECTA CON TU NUEVO BOTÓN
    @PostMapping("/usuarios/estado/{id}")
    public String cambiarEstadoUsuario(@PathVariable Long id) {
        Usuario usuario = usuarioService.obtenerPorId(id);
        
        // Usamos setHabilitado porque así se llama en tu clase Model Usuario
        usuario.setHabilitado(!usuario.isHabilitado()); 
        
        usuarioService.save(usuario);
        // Redirigimos a /usuarios que es tu ruta de listado
        return "redirect:/usuarios"; 
    }

    // ---------------------------------------------------------
    // 4. EXPORTAR A PDF
    // ---------------------------------------------------------

    @GetMapping("/usuarios/exportarPDF")
    public void exportarListadoDeUsuariosEnPDF(@RequestParam(name = "keyword", required = false) String keyword, 
                                            HttpServletResponse response) throws IOException, DocumentException {
        
        response.setContentType("application/pdf");
        
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateTime = dateFormatter.format(new Date());
        
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=usuarios_" + currentDateTime + ".pdf";
        response.setHeader(headerKey, headerValue);

        // LÓGICA DE FILTRADO
        List<Usuario> listaUsuarios;

        if (keyword != null && !keyword.isEmpty()) {
            // Si el admin filtró por nombre/email/documento, usamos ese filtro para el PDF
            listaUsuarios = usuarioService.listarUsuarios(keyword); 
        } else {
            // Si no hay filtro, exportamos todo como antes
            listaUsuarios = usuarioService.listarUsuarios();
        }

        UsuarioExporterPDF exporter = new UsuarioExporterPDF(listaUsuarios);
        exporter.exportar(response);
    }

    @PostMapping("/admin/instituciones/{id}/estado")
    @ResponseBody
    public Map<String, Object> aprobarRechazarInstitucion(@PathVariable Long id, @RequestParam String accion) {
        Map<String, Object> response = new HashMap<>();
        try {
            Usuario institucion = usuarioService.obtenerPorId(id);
            if (institucion == null || !"INSTITUCION".equals(institucion.getRol())) {
                response.put("success", false);
                response.put("message", "Institucion no encontrada");
                return response;
            }
            if ("APROBAR".equals(accion)) {
                institucion.setEstadoCuenta("ACTIVO");
                response.put("message", "Institucion aprobada correctamente");
            } else if ("RECHAZAR".equals(accion)) {
                institucion.setEstadoCuenta("RECHAZADO");
                response.put("message", "Institucion rechazada");
            } else {
                response.put("success", false);
                response.put("message", "Accion invalida");
                return response;
            }
            usuarioService.save(institucion);
            response.put("success", true);
            response.put("id", id);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        return response;
    }
}