package com.proyecto.AccesoUsuarios.services;

import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio encargado de la gestión de contraseñas y recuperación de cuentas.
 * Coordina la lógica de negocio entre el backend en Java y el microservicio de Python.
 */
@Service
public class PassswordServices {

    @Autowired
    private RestTemplate restTemplate;
    // Dependencia para hacer peticiones HTTP al microservicio de Python

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${python.api.url:http://localhost:5000}")
    private String pythonApiBaseUrl;

    public void solicitarRestablecimiento(String email) {
        // 1. Buscar al usuario por email (toma el primero si hay duplicados)
        Optional<Usuario> usuarioOpt = usuarioRepository.findFirstByEmail(email);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            // 2. Generar y guardar el token en la base de datos
            String token = UUID.randomUUID().toString();//Es un estándar que garantiza que la probabilidad de que dos tokens sean iguales sea prácticamente cero, haciendo que el enlace de recuperación sea imposible de adivinar por un atacante.
            usuario.setTokenRecuperacion(token);
            usuarioRepository.save(usuario);

            // 3. Preparar el link para el correo
            String linkDeRecuperacion = "http://localhost:8080/auth/reset-password?token=" + token;
            //construccion de link que envia al servidor de python para que este lo incluya en el correo que se le envia al usuario, el link incluye el token unico generado para cada solicitud de restablecimiento de contraseña

            // 4. Enviar datos a Python
            Map<String, String> datosParaPython = new HashMap<>();
            datosParaPython.put("email", email);
            datosParaPython.put("link", linkDeRecuperacion);

            String pythonApiUrl = pythonApiBaseUrl + "/api/enviar-enlace";

            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);//aqui python se le dice que tipo de datos le vas a enviar al configurar un content tipe como json osea que le vasmo a enviar un texto json
                HttpEntity<Map<String, String>> request = new HttpEntity<>(datosParaPython, headers);//ese http entity es un contenedor que encapsula tanto los datos que quieres enviar (en este caso el mapa con email y link) como las cabeceras que has configurado, para que el microservicio de python pueda entender correctamente la solicitud
                restTemplate.postForEntity(pythonApiUrl, request, String.class);//el postforentity indica que vamos a usar el metodo post el python la direccion a donde va el request el sobre que va preparamos y el string class la respuesta de confirmacion
                System.out.println("Correo de recuperación solicitado para: " + email);
            } catch (Exception e) {//esto es en caso de que el servido este mal o pagado y lo mostrara en consoola
                System.err.println("Error enviando a Python: " + e.getMessage());
            }
        } else {
            System.err.println("No se encontró usuario con el email: " + email);//este es un filtro preventivo donde si el correo que escribio el usuario si existe
        }
    }

    public boolean cambiarPasswordConToken(String token, String nuevaPassword) {
        // 1. Buscar al usuario que tiene ese token específico
        Optional<Usuario> usuarioOpt = usuarioRepository.findByTokenRecuperacion(token);//el optional es po si existe el usuario o no

        if (usuarioOpt.isPresent()) {//esto es para verificar que el token que llega del link del correo si existe en la base de datos y no es un token inventado o ya usado
            Usuario usuario = usuarioOpt.get();//si existe usa el metodo get para sacarlo del conternedor optional y trabajar con el objeto usuario

            // 2. Encriptar la nueva contraseña
            usuario.setPassword(passwordEncoder.encode(nuevaPassword));

            // 3. Limpiar el token para que no se pueda volver a usar
            usuario.setTokenRecuperacion(null);

            // 4. Guardar cambios
            usuarioRepository.save(usuario);
            return true;
        }
        
        return false; // El token no existe o ya fue usado
    }
}