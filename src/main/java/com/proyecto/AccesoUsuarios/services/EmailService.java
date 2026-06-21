package com.proyecto.AccesoUsuarios.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarEnlaceRecuperacion(String destinatario, String link) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(destinatario);
        mensaje.setSubject("PortalEdu - Recuperacion de Contrasena");
        mensaje.setText("Hola,\n\nHas solicitado restablecer tu contrasena en PortalEdu.\n"
                + "Haz clic en el siguiente enlace para crear una nueva clave:\n\n"
                + link + "\n\n"
                + "Si no solicitaste este cambio, ignora este mensaje.\n\n"
                + "PortalEdu - Equipo de Soporte");
        mailSender.send(mensaje);
    }
}
