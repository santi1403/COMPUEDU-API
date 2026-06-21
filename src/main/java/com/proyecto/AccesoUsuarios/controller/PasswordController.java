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
@RequestMapping("/auth")// el request mapping es para organizar las rutas relacionadas con autenticacion y asi el link del correo se vea mas limpio y organizado con /auth/reset-password?token=abc123 en lugar de tener una ruta larga y desordenada
public class PasswordController {

    @Autowired
    private PassswordServices passwordServices;

    // Esta ruta muestra el formulario HTML
    @GetMapping("/recuperar")
    public String mostrarFormulario() {
        return "recuperar_password"; 
    }

    // Esta es la ruta que se abre cuando el usuario hace clic en el link del correo
    @GetMapping("/reset-password")
    public String mostrarFormularioCambio(@RequestParam("token") String token, Model model) {
        // Pasamos el token al HTML para saber a quién le cambiaremos la clave después
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
            model.addAttribute("error", "El enlace es inválido o ha expirado.");
            return "nueva_password";
        }
    }


    // Esta ruta recibe el dato del formulario
    @PostMapping("/enviar-solicitud")
    public String procesarRecuperacion(@RequestParam("email") String email, Model model) {
        try {
            passwordServices.solicitarRestablecimiento(email);
            model.addAttribute("mensaje", "Si el correo existe, se enviará un enlace.");
        } catch (Exception e) {
            model.addAttribute("error", "Error al procesar la solicitud.");
        }
        return "recuperar_password"; // Volvemos al mismo formulario con un mensaje
    }
}