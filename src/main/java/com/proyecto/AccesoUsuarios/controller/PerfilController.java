package com.proyecto.AccesoUsuarios.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.services.UsuarioService;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    @Autowired
    private UsuarioService usuarioService;

    // 1. Mostrar el formulario con los datos actuales
    @GetMapping
    public String verPerfil(Authentication auth, Model model) {
        String username = auth.getName();
        Usuario usuario = usuarioService.findByUserName(username);
        model.addAttribute("usuario", usuario);
        return "perfil";
    }

    // 2. Procesar la actualización
    @PostMapping("/actualizar")
    public String actualizarPerfil(@ModelAttribute Usuario usuarioForm, Authentication auth, RedirectAttributes redirectAttrs) {
        try {
            String username = auth.getName();
            Usuario usuarioActual = usuarioService.findByUserName(username);

            usuarioActual.setEmail(usuarioForm.getEmail());
            usuarioActual.setTelefono(usuarioForm.getTelefono());
            usuarioActual.setCedula(usuarioForm.getCedula());

            usuarioService.save(usuarioActual);
            redirectAttrs.addFlashAttribute("mensaje", "Perfil actualizado con exito!");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Error al guardar: " + e.getMessage());
        }
        return "redirect:/perfil";
    }
}