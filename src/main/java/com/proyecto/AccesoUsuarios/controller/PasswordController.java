package com.proyecto.AccesoUsuarios.controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.proyecto.AccesoUsuarios.services.PassswordServices;

import org.springframework.beans.factory.annotation.Autowired;

@Controller
@RequestMapping("/auth")
public class PasswordController {

    @Autowired
    private PassswordServices passwordServices;

    @GetMapping("/recuperar")
    public String mostrarFormulario() {
        return "recuperar_password";
    }

    @GetMapping("/reset-password")
    public String mostrarFormularioCambio(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        return "nueva_password";
    }

    @PostMapping("/actualizar-password")
    public String actualizarPassword(@RequestParam("token") String token,
                                    @RequestParam("password") String password,
                                    Model model) {
        boolean exito = passwordServices.cambiarPasswordConToken(token, password);

        if (exito) {
            return "redirect:/login?resetSuccess";
        } else {
            model.addAttribute("error", "El enlace es invalido o ha expirado.");
            return "nueva_password";
        }
    }

    @PostMapping("/enviar-solicitud")
    public String procesarRecuperacion(@RequestParam("email") String email, Model model) {
        String link = passwordServices.solicitarRestablecimiento(email);

        if (link != null) {
            model.addAttribute("link", link);
            return "confirmacion-recuperacion";
        } else {
            model.addAttribute("error", "No se encontro una cuenta con ese correo.");
            return "recuperar_password";
        }
    }
}
