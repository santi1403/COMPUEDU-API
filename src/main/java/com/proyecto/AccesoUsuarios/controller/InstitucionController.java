package com.proyecto.AccesoUsuarios.controller;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.lowagie.text.DocumentException;
import com.proyecto.AccesoUsuarios.Utils.HojaVidaExporterPDF;
import com.proyecto.AccesoUsuarios.model.Convocatoria;
import com.proyecto.AccesoUsuarios.model.Inscripcion;
import com.proyecto.AccesoUsuarios.model.Notificacion;
import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.repository.ConvocatoriaRepository;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
import com.proyecto.AccesoUsuarios.repository.InscripcionRepository;
import com.proyecto.AccesoUsuarios.repository.NotificacionRepository;

import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/institucion")
public class InstitucionController {

    @Autowired private UsuarioRepository usuarioRepo;
    @Autowired private ConvocatoriaRepository convocatoriaRepo;
    @Autowired private InscripcionRepository inscripcionRepo;
    @Autowired private NotificacionRepository notificacionRepo;

    @GetMapping("/dashboard")
    public String dashboardInstitucion(Authentication auth, Model model) {
        try {
            // 1. Identificación dinámica del usuario
            String username = auth.getName();
            Usuario institucion = usuarioRepo.findByUserName(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            Long idDinamico = institucion.getId();
            System.out.println("========== DASHBOARD START ==========");
            System.out.println(">>> Usuario: " + username + " | ID: " + idDinamico);

            // 2. Datos locales (Java)
            model.addAttribute("nombreUsuario", username);
            model.addAttribute("misConvocatoriasCount", convocatoriaRepo.countByCreador(institucion));
            model.addAttribute("misInscritosCount", inscripcionRepo.countByConvocatoria_Creador(institucion));
            model.addAttribute("ultimosInscritos", inscripcionRepo.findByConvocatoria_Creador(institucion));

            // Datos del grafico desde MySQL
            long pendientes = inscripcionRepo.countByConvocatoria_CreadorAndEstado(institucion, "PENDIENTE");
            long aceptadas = inscripcionRepo.countByConvocatoria_CreadorAndEstado(institucion, "ACEPTADA");
            long rechazadas = inscripcionRepo.countByConvocatoria_CreadorAndEstado(institucion, "RECHAZADA");
            model.addAttribute("chartPendientes", pendientes);
            model.addAttribute("chartAceptadas", aceptadas);
            model.addAttribute("chartRechazadas", rechazadas);

            // Alertas locales: convocatorias que cierran en 7 dias
            java.time.LocalDate limite = java.time.LocalDate.now().plusDays(7);
            model.addAttribute("alertasCierre", convocatoriaRepo.findProximasACerrar(institucion, limite));
            
            System.out.println("========== DASHBOARD END ==========");

        } catch (Exception e) {
            System.err.println(">>> ERROR GENERAL EN DASHBOARD: " + e.getMessage());
            e.printStackTrace();
        }

        return "dashboard/institucion";
    }

        @GetMapping("/mis-convocatorias") 
            public String listarMisConvocatorias(Model model, Authentication auth) {
                String username = auth.getName();
                Usuario institucion = usuarioRepo.findByUserName(username).orElseThrow();
                
                // Buscamos las convocatorias creadas por esta institución
                // (Asegúrate de haber agregado findByCreador en tu repositorio como vimos antes)
                model.addAttribute("convocatorias", convocatoriaRepo.findByCreador(institucion));
                model.addAttribute("nombreUsuario", institucion.getUserName());
                
                // Busca el archivo en templates/institucion/mis_convocatorias.html
                return "institucion/mis_convocatorias";
            }

        @PostMapping("/convocatorias/toggle/{id}")
            public String toggleEstadoConvocatoria(@PathVariable Long id, Authentication auth) {
                String username = auth.getName();
                Usuario institucion = usuarioRepo.findByUserName(username).orElseThrow();
                
                // 1. Buscamos la convocatoria asegurándonos de que sea de esta institución
                // Usamos el repositorio de convocatorias
                Convocatoria conv = convocatoriaRepo.findById(id)
                        .filter(c -> c.getCreador().getId().equals(institucion.getId()))
                        .orElseThrow(() -> new RuntimeException("No autorizado"));

                // 2. Solo permitimos cambiar si no está ANULADA por el administrador
                if (!"ANULADA".equals(conv.getEstado())) {
                    if ("ACTIVA".equals(conv.getEstado())) {
                        conv.setEstado("INACTIVA");
                        conv.setActiva(false);
                    } else {
                        conv.setEstado("ACTIVA");
                        conv.setActiva(true);
                    }
                    convocatoriaRepo.save(conv);
                }

                return "redirect:/institucion/mis-convocatorias";
        }

        @PostMapping("/inscripciones/{id}/estado")
        @ResponseBody
        public Map<String, Object> cambiarEstadoInscripcion(@PathVariable Long id,
                                                             @RequestParam String estado,
                                                             Authentication auth) {
            Map<String, Object> response = new HashMap<>();
            
            try {
                String username = auth.getName();
                Usuario institucion = usuarioRepo.findByUserName(username).orElseThrow();
                
                Inscripcion inscripcion = inscripcionRepo.findById(id).orElse(null);
                if (inscripcion == null) {
                    response.put("success", false);
                    response.put("message", "Inscripcion no encontrada");
                    return response;
                }
                
                if (!inscripcion.getConvocatoria().getCreador().getId().equals(institucion.getId())) {
                    response.put("success", false);
                    response.put("message", "No autorizado: esta inscripcion no pertenece a tus convocatorias");
                    return response;
                }
                
                if (!"ACEPTADA".equals(estado) && !"RECHAZADA".equals(estado)) {
                    response.put("success", false);
                    response.put("message", "Estado invalido. Use ACEPTADA, RECHAZADA o ENTREVISTA");
                    return response;
                }
                
                inscripcion.setEstado(estado);
                inscripcionRepo.save(inscripcion);
                
                Notificacion noti = new Notificacion();
                String accion = "ACEPTADA".equals(estado) ? "aceptado" : "rechazado";
                noti.setMensaje("Has sido " + accion + " en la convocatoria: " + inscripcion.getConvocatoria().getTitulo());
                noti.setUsuario(inscripcion.getUsuario());
                noti.setConvocatoria(inscripcion.getConvocatoria());
                notificacionRepo.save(noti);
                
                response.put("success", true);
                response.put("message", "Inscripcion " + estado.toLowerCase() + " correctamente");
                response.put("estado", estado);
                response.put("id", id);
                
            } catch (Exception e) {
                response.put("success", false);
                response.put("message", "Error: " + e.getMessage());
            }
            
            return response;
        }

        @GetMapping("/postulantes/eliminar/{id}")
        public String eliminarPostulante(@PathVariable Long id, Authentication auth) {
            String username = auth.getName();
            Usuario institucion = usuarioRepo.findByUserName(username).orElseThrow();

            Inscripcion ins = inscripcionRepo.findById(id).orElseThrow();
            if (ins.getConvocatoria().getCreador().getId().equals(institucion.getId())) {
                inscripcionRepo.deleteById(id);
            }
            return "redirect:/institucion/dashboard";
        }

        @GetMapping("/reporte-postulantes/pdf")
        public void reportePostulantesPDF(Authentication auth, HttpServletResponse response) throws IOException, DocumentException {
            String username = auth.getName();
            Usuario institucion = usuarioRepo.findByUserName(username).orElseThrow();
            List<Inscripcion> inscripciones = inscripcionRepo.findByConvocatoria_Creador(institucion);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=reporte_postulantes.pdf");

            HojaVidaExporterPDF exporter = new HojaVidaExporterPDF(inscripciones,
                    institucion.getNombreInstitucion() != null ? institucion.getNombreInstitucion() : username);
            exporter.exportar(response);
        }

}