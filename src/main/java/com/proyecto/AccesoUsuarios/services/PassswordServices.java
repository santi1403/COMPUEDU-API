package com.proyecto.AccesoUsuarios.services;

import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class PassswordServices {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    public void solicitarRestablecimiento(String email) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findFirstByEmail(email);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            String token = UUID.randomUUID().toString();
            usuario.setTokenRecuperacion(token);
            usuarioRepository.save(usuario);

            String linkDeRecuperacion = "http://localhost:8080/auth/reset-password?token=" + token;

            try {
                emailService.enviarEnlaceRecuperacion(email, linkDeRecuperacion);
                System.out.println("Correo de recuperacion enviado a: " + email);
            } catch (Exception e) {
                System.err.println("Error enviando correo: " + e.getMessage());
            }
        } else {
            System.err.println("No se encontro usuario con el email: " + email);
        }
    }

    public boolean cambiarPasswordConToken(String token, String nuevaPassword) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByTokenRecuperacion(token);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            usuario.setPassword(passwordEncoder.encode(nuevaPassword));
            usuario.setTokenRecuperacion(null);
            usuarioRepository.save(usuario);
            return true;
        }

        return false;
    }
}