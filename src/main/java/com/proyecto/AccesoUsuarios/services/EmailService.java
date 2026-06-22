package com.proyecto.AccesoUsuarios.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class EmailService {

    @Value("${mailtrap.api.url:https://sandbox.api.mailtrap.io/api/send}")
    private String mailtrapApiUrl;

    @Value("${mailtrap.api.token}")
    private String mailtrapApiToken;

    private final RestTemplate restTemplate = new RestTemplate();

    public void enviarEnlaceRecuperacion(String destinatario, String link) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + mailtrapApiToken);

        Map<String, Object> body = Map.of(
            "from", Map.of("email", "noreply@portaledu.com", "name", "PortalEdu"),
            "to", new Object[]{ Map.of("email", destinatario) },
            "subject", "PortalEdu - Recuperacion de Contrasena",
            "text", "Hola,\n\nHas solicitado restablecer tu contrasena en PortalEdu.\n"
                    + "Haz clic en el siguiente enlace para crear una nueva clave:\n\n"
                    + link + "\n\n"
                    + "Si no solicitaste este cambio, ignora este mensaje.\n\n"
                    + "PortalEdu - Equipo de Soporte",
            "category", "Password Recovery"
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(mailtrapApiUrl, request, String.class);
    }
}
