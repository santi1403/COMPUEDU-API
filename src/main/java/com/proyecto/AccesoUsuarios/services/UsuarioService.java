package com.proyecto.AccesoUsuarios.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository repo;

    public UsuarioService(UsuarioRepository repo) {
        this.repo = repo;
    }

    // 1. Guardar usuario (Sirve para crear y actualizar)
    public void save(Usuario usuario) {
        repo.save(usuario);
    }

    // 2. Listar todos (Usado en el panel Admin y PDF)
    public List<Usuario> listarUsuarios() {
        return repo.findAll();
    }

    // NUEVO MÉTODO: Para listar con filtro
    public List<Usuario> listarUsuarios(String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            return repo.findAll(keyword); 
        }
        return repo.findAll();
    }

    // 3. Obtener un usuario por su ID (Usado para editar)
    public Usuario obtenerPorId(Long id) {
        return repo.findById(id).orElse(null); // Devuelve null si no existe
    }

    // 4. Eliminar usuario
    public void eliminar(Long id) {
        repo.deleteById(id);
    }
    // --- TU MÉTODO DE FILTRADO (ADAPTADO) ---
    /* NOTA: He comentado las partes de fechaRegistro y getNombre 
       porque en tu Entidad Usuario tienes 'userName' y quizás no tengas 'fechaRegistro'.
       Si agregas esos campos a la entidad, puedes descomentar esto.
    */
    public List<Usuario> filtrarUsuarios(String keyword, String rol) {
        // Si el rol es "Todos", lo mandamos como null para que la Query lo ignore
        String rolBusqueda = (rol == null || rol.equals("Todos") || rol.isEmpty()) ? null : rol;
        
        // Si la keyword está vacía, la mandamos como null
        String keywordBusqueda = (keyword == null || keyword.isEmpty()) ? null : keyword;

        return repo.filtrarUsuarios(keywordBusqueda, rolBusqueda);
    }

    // Agrega este método:
    public boolean existsByUserName(String userName) {
        return repo.existsByUserName(userName);
    }

    public void cambiarEstadoUsuario(Long id) {
        Usuario usuario = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Bloqueo de seguridad para el admin principal
        if (usuario.getUserName().equals("manny")) {
            throw new RuntimeException("No está permitido inhabilitar al administrador principal del sistema.");
        }
        // Lógica normal para los demás
        usuario.setHabilitado(!usuario.isHabilitado());
        repo.save(usuario);
    }
}

