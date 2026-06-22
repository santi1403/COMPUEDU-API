package com.proyecto.AccesoUsuarios.services;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class EmailService {

    private static final String MAILTRAP_URL = "https://sandbox.api.mailtrap.io/api/send/boxes/4727911/emails";
    private static final String API_TOKEN = "0884ed46f85f5d50d0cbb8ff3e10062b";

    private final RestTemplate restTemplate = new RestTemplate();

    public void enviarEnlaceRecuperacion(String destinatario, String link) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Api-Token", API_TOKEN);

            Map<String, Object> body = Map.of(
                "from", Map.of("email", "noreply@portaledu.com", "name", "PortalEdu"),
                "to", new Object[]{ Map.of("email", destinatario) },
                "subject", "PortalEdu - Recuperacion de Contrasena",
                "text", "Hola,\n\nHas solicitado restablecer tu contrasena en PortalEdu.\n"
                        + "Haz clic en el siguiente enlace para crear una nueva clave:\n\n"
                        + link + "\n\n"
                        + "Si no solicitaste este cambio, ignora este mensaje.\n\n"
                        + "PortalEdu - Equipo de Soporte"
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(MAILTRAP_URL, request, String.class);
            System.out.println("Correo enviado exitosamente a: " + destinatario);

        } catch (Exception e) {
            System.err.println("Error al enviar correo: " + e.getMessage());
        }
    }
}
