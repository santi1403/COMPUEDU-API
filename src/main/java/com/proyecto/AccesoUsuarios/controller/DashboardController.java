package com.proyecto.AccesoUsuarios.controller;


import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.lowagie.text.DocumentException;
import com.proyecto.AccesoUsuarios.Utils.ReporteGlobalExporterPDF;
import com.proyecto.AccesoUsuarios.model.Inscripcion;
import com.proyecto.AccesoUsuarios.model.Notificacion;
import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.repository.ConvocatoriaRepository;
import com.proyecto.AccesoUsuarios.repository.InscripcionRepository;
import com.proyecto.AccesoUsuarios.repository.NotificacionRepository;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class DashboardController {

    @Autowired private NotificacionRepository notificacionRepo;
    @Autowired private UsuarioRepository usuarioRepo;
    @Autowired private ConvocatoriaRepository convocatoriaRepo;
    @Autowired private InscripcionRepository inscripcionRepo;

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        String username = auth.getName();
        Usuario usuario = usuarioRepo.findByUserName(username).orElseThrow(); 

        // -----------------------------------------------------------
        // 1. LÓGICA PARA EL ADMINISTRADOR
        // -----------------------------------------------------------
        if ("ADMIN".equals(usuario.getRol()) || auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            
            model.addAttribute("totalUsuarios", usuarioRepo.count());
            model.addAttribute("usuariosPendientes", usuarioRepo.countByHabilitadoFalse());
            model.addAttribute("convocatoriasActivas", convocatoriaRepo.count());
            model.addAttribute("totalInscripciones", inscripcionRepo.count());
            model.addAttribute("institucionesPendientes", usuarioRepo.findByRolAndEstadoCuenta("INSTITUCION", "PENDIENTE"));

            model.addAttribute("chartPendientes", inscripcionRepo.countByEstado("PENDIENTE"));
            model.addAttribute("chartAceptadas", inscripcionRepo.countByEstado("ACEPTADA"));
            model.addAttribute("chartRechazadas", inscripcionRepo.countByEstado("RECHAZADA"));

            List<Map<String, String>> bitacora = new ArrayList<>();
            List<Inscripcion> ultimas = inscripcionRepo.findTop6ByOrderByIdDesc();
            for (Inscripcion ins : ultimas) {
                Map<String, String> entry = new HashMap<>();
                entry.put("fecha", ins.getFechaInscripcion().toString());
                entry.put("tipo", "Inscripcion");
                entry.put("desc", ins.getUsuario().getUserName() + " se postulo a " + ins.getConvocatoria().getTitulo());
                bitacora.add(entry);
            }
            model.addAttribute("bitacora", bitacora);

            return "dashboard/admin"; 
        }
        
        // -----------------------------------------------------------
        // 2. LÓGICA PARA LA INSTITUCIÓN (NUEVO)
        // -----------------------------------------------------------
        // 2. LÓGICA INSTITUCIÓN
        // Dentro de DashboardController.java
            else if ("INSTITUCION".equals(usuario.getRol()) || auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_INSTITUCION"))) {
                if ("PENDIENTE".equals(usuario.getEstadoCuenta())) {
                    return "redirect:/registro/institucion/espera";
                }
                if ("RECHAZADO".equals(usuario.getEstadoCuenta())) {
                    return "redirect:/registro/institucion/rechazo";
                }
                return "redirect:/institucion/dashboard"; 
            }

        // -----------------------------------------------------------
        // 3. LÓGICA PARA EL ESTUDIANTE (USUARIO)
        // -----------------------------------------------------------
        else {
            List<Notificacion> notificaciones = notificacionRepo.findByUsuarioAndLeidaFalse(usuario);
            
            // 3. PÁSALAS AL MODELO (Asegúrate que los nombres coincidan con tu HTML)
            model.addAttribute("notificaciones", notificaciones);
            model.addAttribute("notificacionesNoLeidas", notificaciones.size());


            model.addAttribute("misInscripciones", inscripcionRepo.findByUsuario(usuario));
            model.addAttribute("totalMisInscripciones", inscripcionRepo.findByUsuario(usuario).size());
            model.addAttribute("misAceptadas", inscripcionRepo.findByUsuario(usuario).stream().filter(i -> "ACEPTADA".equals(i.getEstado())).count());
            model.addAttribute("misRechazadas", inscripcionRepo.findByUsuario(usuario).stream().filter(i -> "RECHAZADA".equals(i.getEstado())).count());
            model.addAttribute("convocatoriasDisponibles", convocatoriaRepo.findByEstado("APROBADA"));
            model.addAttribute("convocatoriasDestacadas", convocatoriaRepo.findTop3ByEstadoOrderByIdDesc("APROBADA"));
            model.addAttribute("nombreUsuario", usuario.getUserName());

            // Redirige al archivo: src/main/resources/templates/home.html
            // (IMPORTANTE: Mantenlo como "home", no "dashboard/home" si tu archivo está en la raíz templates)
            return "home"; 
        }
    }

    @GetMapping("/admin/reporte-global/pdf")
    public void reporteGlobalPDF(HttpServletResponse response) throws IOException, DocumentException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=reporte_global_portaledu.pdf");

        long usuarios = usuarioRepo.count();
        long instituciones = usuarioRepo.findByRolAndEstadoCuenta("INSTITUCION", "ACTIVO").size()
                + usuarioRepo.findByRolAndEstadoCuenta("INSTITUCION", "PENDIENTE").size();
        long convocatorias = convocatoriaRepo.count();
        long inscripciones = inscripcionRepo.count();
        long pendientes = inscripcionRepo.countByEstado("PENDIENTE");
        long aceptadas = inscripcionRepo.countByEstado("ACEPTADA");
        long rechazadas = inscripcionRepo.countByEstado("RECHAZADA");

        ReporteGlobalExporterPDF exporter = new ReporteGlobalExporterPDF(
                usuarios, instituciones, convocatorias, inscripciones, pendientes, aceptadas, rechazadas);
        exporter.exportar(response);
    }
}