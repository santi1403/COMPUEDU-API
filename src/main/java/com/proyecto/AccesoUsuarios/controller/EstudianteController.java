package com.proyecto.AccesoUsuarios.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.proyecto.AccesoUsuarios.model.FiltroEstudiante;
import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.repository.FiltroEstudianteRepository;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;

@Controller
@RequestMapping("/estudiante")
public class EstudianteController {

    @Autowired
    private FiltroEstudianteRepository filtroRepo;
    
    @Autowired
    private UsuarioRepository usuarioRepo;

    @PostMapping("/guardar-alerta")
    public String guardarAlerta(@RequestParam String palabraClave, 
                                @RequestParam String categoria, 
                                Authentication auth) {
        
        // Obtenemos el nombre del usuario logueado actualmente
        String username = auth.getName();
        
        // Buscamos el objeto Usuario completo en la DB
        Usuario usuario = usuarioRepo.findByUserName(username)
                            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Creamos la nueva alerta vinculada a ese usuario
        FiltroEstudiante nuevoFiltro = new FiltroEstudiante();
        nuevoFiltro.setPalabraClave(palabraClave);
        nuevoFiltro.setCategoria(categoria);
        nuevoFiltro.setUsuario(usuario);

        // Guardamos en la tabla filtros_estudiante
        filtroRepo.save(nuevoFiltro);

        return "redirect:/dashboard?alertaGuardada=true";
    }

    @GetMapping("/ver-curso")
    public String verCurso(@RequestParam("tipo") String tipo, Model model) {
        if ("logica".equals(tipo)) {
            model.addAttribute("titulo", "Taller de Logica de Programacion");
            model.addAttribute("contenido", "Bienvenido al Taller de Logica de Programacion.\n\n"
                + "En este curso aprenderas los fundamentos del pensamiento algoritmico: variables, condicionales, "
                + "bucles y estructuras de datos basicas. Trabajaremos con pseudocodigo y diagramas de flujo para "
                + "que desarrolles la capacidad de resolver problemas de forma estructurada.\n\n"
                + "Contenido del curso:\n"
                + "- Introduccion a la logica computacional\n"
                + "- Variables y tipos de datos\n"
                + "- Estructuras condicionales (if/else)\n"
                + "- Bucles (for, while)\n"
                + "- Arreglos y listas\n"
                + "- Funciones y modularidad\n\n"
                + "Duracion estimada: 4 semanas. Modalidad: Virtual autogestionable.");
        } else if ("lectura".equals(tipo)) {
            model.addAttribute("titulo", "Curso de Comprension Lectora");
            model.addAttribute("contenido", "Bienvenido al Curso de Comprension Lectora.\n\n"
                + "Este curso esta disenado para fortalecer tus habilidades de lectura critica, analisis de textos "
                + "y comprension de ideas principales y secundarias. Es ideal para prepararte para pruebas de admision "
                + "universitarias como el ICFES Saber 11 o examenes de ingreso.\n\n"
                + "Contenido del curso:\n"
                + "- Tecnicas de lectura rapida\n"
                + "- Identificacion de ideas principales\n"
                + "- Analisis de argumentos y falacias\n"
                + "- Comprension de textos cientificos\n"
                + "- Interpretacion de graficos y tablas\n"
                + "- Practica con preguntas tipo ICFES\n\n"
                + "Duracion estimada: 3 semanas. Modalidad: Virtual autogestionable.");
        } else {
            model.addAttribute("titulo", "Introduccion a las Matematicas");
            model.addAttribute("contenido", "Bienvenido a Introduccion a las Matematicas.\n\n"
                + "Este curso cubre los conceptos fundamentales de matematicas necesarios para el ingreso a la "
                + "educacion superior. Desde teoria de conjuntos hasta algebra basica, te preparamos para que "
                + "llegues con una base solida a tu primer semestre universitario.\n\n"
                + "Contenido del curso:\n"
                + "- Teoria de conjuntos\n"
                + "- Operaciones basicas y propiedades\n"
                + "- Fracciones y decimales\n"
                + "- Potenciacion y radicacion\n"
                + "- Expresiones algebraicas\n"
                + "- Ecuaciones de primer y segundo grado\n"
                + "- Introduccion a funciones\n\n"
                + "Duracion estimada: 5 semanas. Modalidad: Virtual autogestionable.");
        }
        return "vista-curso";
    }
}

