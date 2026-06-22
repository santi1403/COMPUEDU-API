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

    public String solicitarRestablecimiento(String email) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findFirstByEmail(email);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            String token = UUID.randomUUID().toString();
            usuario.setTokenRecuperacion(token);
            usuarioRepository.save(usuario);

            String linkDeRecuperacion = "http://localhost:8080/auth/reset-password?token=" + token;

            // Intenta enviar correo en segundo plano (no bloquea)
            emailService.enviarEnlaceRecuperacion(email, linkDeRecuperacion);

            return linkDeRecuperacion;
        }
        return null;
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
