package com.proyecto.AccesoUsuarios.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.MalformedURLException;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity; // Para mensajes de éxito

import com.lowagie.text.DocumentException;
import com.proyecto.AccesoUsuarios.Utils.ComprobanteExporterPDF;
import com.proyecto.AccesoUsuarios.Utils.HojaVidaExporterPDF;
import com.proyecto.AccesoUsuarios.model.Convocatoria;
import com.proyecto.AccesoUsuarios.model.FiltroEstudiante;
import com.proyecto.AccesoUsuarios.model.Inscripcion;
import com.proyecto.AccesoUsuarios.model.Notificacion;
import com.proyecto.AccesoUsuarios.model.Usuario;
import com.proyecto.AccesoUsuarios.repository.ConvocatoriaRepository;
import com.proyecto.AccesoUsuarios.repository.FiltroEstudianteRepository;
import com.proyecto.AccesoUsuarios.repository.InscripcionRepository;
import com.proyecto.AccesoUsuarios.repository.NotificacionRepository;
import com.proyecto.AccesoUsuarios.repository.UsuarioRepository;
import com.proyecto.AccesoUsuarios.services.ConvocatoriaService;


import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/convocatorias")
public class ConvocatoriaController {

    @Autowired private ConvocatoriaRepository convocatoriaRepo;
    @Autowired private InscripcionRepository inscripcionRepo;
    @Autowired private UsuarioRepository usuarioRepo;
    @Autowired private ConvocatoriaService convocatoriaService;
    @Autowired private FiltroEstudianteRepository filtroRepo;
    @Autowired private NotificacionRepository notificacionRepo;

    public ConvocatoriaController(ConvocatoriaService convocatoriaService) {
        this.convocatoriaService = convocatoriaService;
    }

    // --- SECCION ADMIN ---
    @GetMapping("/nueva")
    public String nuevaConvocatoria(Model model) {
        model.addAttribute("convocatoria", new Convocatoria());
        return "convocatorias/form_admin"; 
    }

    @PostMapping("/guardar") // Esta es la ruta que tienes en el HTML
    public String guardarConvocatoriaUnificada(@ModelAttribute Convocatoria convocatoria, Authentication auth) {
        try {
            // 1. OBTENER EL CREADOR (Institución o Admin)
            String username = auth.getName();
            Usuario creador = usuarioRepo.findByUserName(username).orElseThrow();
            
            // 2. CONFIGURAR DATOS INICIALES
            convocatoria.setCreador(creador);
            convocatoria.setActiva(true); 
            convocatoria.setEstado("APROBADA");

            // 3. GUARDAR LA CONVOCATORIA PRIMERO
            convocatoriaRepo.save(convocatoria);
            System.out.println(">>> Convocatoria guardada por: " + creador.getUserName());

            // 4. LÓGICA DE NOTIFICACIONES (El Match)
            List<FiltroEstudiante> filtros = filtroRepo.findAll();
            String tituloNormalizado = normalizarTexto(convocatoria.getTitulo());

            for (FiltroEstudiante filtro : filtros) {
                // Verificamos si la categoría coincide
                if (convocatoria.getCategoria() != null && 
                    convocatoria.getCategoria().equalsIgnoreCase(filtro.getCategoria())) {
                    
                    String palabrasFiltro = normalizarTexto(filtro.getPalabraClave());
                    
                    // Verificamos si el título contiene la palabra clave
                    if (tituloNormalizado.contains(palabrasFiltro)) {
                        Notificacion n = new Notificacion();
                        n.setMensaje("¡Nueva oportunidad para ti! " + convocatoria.getTitulo());
                        n.setUsuario(filtro.getUsuario());
                        n.setConvocatoria(convocatoria);
                        n.setLeida(false);
                        n.setFechaCreacion(LocalDateTime.now());
                        
                        notificacionRepo.save(n);
                        System.out.println(">>> NOTIFICACIÓN CREADA para: " + filtro.getUsuario().getUserName());
                    }
                }
            }

            // 5. REDIRECCIÓN SEGÚN ROL
            if ("INSTITUCION".equals(creador.getRol())) {
                return "redirect:/institucion/mis-convocatorias";
            }
            return "redirect:/dashboard";

        } catch (Exception e) {
            System.err.println(">>> ERROR: " + e.getMessage());
            return "redirect:/dashboard?error";
        }
    }

    @GetMapping("/notificaciones/leer/{id}")
    public String marcarComoLeida(@PathVariable Long id) {
        Notificacion noti = notificacionRepo.findById(id).orElseThrow();
        noti.setLeida(true); // Cambiamos el estado
        notificacionRepo.save(noti); // Guardamos el cambio en MySQL
        
        // Redirigimos a la página de convocatorias para que el usuario vea el detalle
        return "redirect:/convocatorias/disponibles";
    }

    // --- SECCION USUARIO ---

    // 1. LISTAR DISPONIBLES (Con lógica de verificación)
    @GetMapping("/disponibles")
    public String listarDisponibles(Model model, Authentication auth,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String modalidad) {

        List<Convocatoria> convocatorias;
        if ((keyword != null && !keyword.trim().isEmpty()) || (categoria != null && !categoria.isEmpty())
                || (area != null && !area.isEmpty()) || (modalidad != null && !modalidad.isEmpty())) {
            convocatorias = convocatoriaRepo.buscarConFiltrosAvanzados(
                    keyword != null && !keyword.trim().isEmpty() ? keyword.trim() : null,
                    categoria != null && !categoria.isEmpty() ? categoria : null,
                    area != null && !area.isEmpty() ? area : null,
                    modalidad != null && !modalidad.isEmpty() ? modalidad : null);
        } else {
            convocatorias = convocatoriaRepo.findByEstado("APROBADA");
        }
        
        // ... (El resto del código de inscripcionesIds sigue igual) ...
        String username = auth.getName();
        Usuario usuario = usuarioRepo.findByUserName(username).orElseThrow();
        List<Inscripcion> misInscripciones = inscripcionRepo.findByUsuario(usuario);
        List<Long> inscripcionesIds = misInscripciones.stream()
                .map(i -> i.getConvocatoria().getId())
                .collect(Collectors.toList());

        Map<Long, String> inscripcionesEstados = new HashMap<>();
        Map<Long, Long> cuposOcupados = new HashMap<>();
        for (Inscripcion ins : misInscripciones) {
            inscripcionesEstados.put(ins.getConvocatoria().getId(), ins.getEstado());
        }
        for (Convocatoria c : convocatorias) {
            cuposOcupados.put(c.getId(), inscripcionRepo.countByConvocatoria(c));
        }

        model.addAttribute("convocatorias", convocatorias);
        model.addAttribute("inscripcionesIds", inscripcionesIds);
        model.addAttribute("inscripcionesEstados", inscripcionesEstados);
        model.addAttribute("cuposOcupados", cuposOcupados);

        return "convocatorias/lista_usuario";
    }

    // 2. INSCRIBIRSE (Con mensajes de confirmación)
    @PostMapping("/inscribirse/{id}")
    public String inscribirse(@PathVariable Long id, Authentication auth, RedirectAttributes redirectAttrs,
            @RequestParam(value = "fileIcfes", required = false) MultipartFile fileIcfes,
            @RequestParam(value = "fileIdentidad", required = false) MultipartFile fileIdentidad) {
        String username = auth.getName();
        Usuario usuario = usuarioRepo.findByUserName(username).orElseThrow();
        Convocatoria conv = convocatoriaRepo.findById(id).orElseThrow();

        if (!inscripcionRepo.existsByUsuarioAndConvocatoria(usuario, conv)) {

            long inscritos = inscripcionRepo.countByConvocatoria(conv);
            if (inscritos >= conv.getCupos()) {
                redirectAttrs.addFlashAttribute("error", "Lo sentimos, los cupos para esta convocatoria ya estan llenos.");
                return "redirect:/convocatorias/disponibles";
            }

            Inscripcion ins = new Inscripcion();
            ins.setUsuario(usuario);
            ins.setConvocatoria(conv);
            ins.setFechaInscripcion(LocalDateTime.now());

            try {
                Path uploadDir = Paths.get("uploads");
                if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir);

                if (fileIcfes != null && !fileIcfes.isEmpty()) {
                    String nombreIcfes = System.currentTimeMillis() + "_icfes_" + fileIcfes.getOriginalFilename();
                    Files.copy(fileIcfes.getInputStream(), uploadDir.resolve(nombreIcfes));
                    ins.setPdfIcfes(nombreIcfes);
                }
                if (fileIdentidad != null && !fileIdentidad.isEmpty()) {
                    String nombreId = System.currentTimeMillis() + "_id_" + fileIdentidad.getOriginalFilename();
                    Files.copy(fileIdentidad.getInputStream(), uploadDir.resolve(nombreId));
                    ins.setPdfIdentidad(nombreId);
                }
            } catch (IOException e) {
                redirectAttrs.addFlashAttribute("error", "Error al subir archivos.");
                return "redirect:/convocatorias/disponibles";
            }

            inscripcionRepo.save(ins);
            redirectAttrs.addFlashAttribute("mensaje", "Inscripcion exitosa!");
        } else {
            redirectAttrs.addFlashAttribute("error", "Ya estabas inscrito en esta convocatoria.");
        }

        return "redirect:/convocatorias/mis-inscripciones";
    }
    
    @GetMapping("/mis-inscripciones")
    public String misInscripciones(Model model, Authentication auth) {
        String username = auth.getName();
        Usuario usuario = usuarioRepo.findByUserName(username).orElseThrow();
        model.addAttribute("inscripciones", inscripcionRepo.findByUsuario(usuario));
        return "convocatorias/mis_inscripciones";
    }

    @GetMapping("/oportunidades")
    public String verOportunidades(@RequestParam(name = "keyword", required = false) String keyword, Model model) {
        List<Convocatoria> lista = convocatoriaService.listarParaEstudiante(keyword);
        
        model.addAttribute("convocatorias", lista);
        model.addAttribute("keyword", keyword);
        
        // AÑADE ESTA LÍNEA para evitar el error de "null object"
        model.addAttribute("inscripcionesIds", new java.util.ArrayList<Long>()); 
        
        return "convocatorias/lista_usuario"; 
    }

    @GetMapping("/comprobante/{id}")
    public void descargarComprobante(@PathVariable Long id, HttpServletResponse response) throws IOException, DocumentException {
        Inscripcion inscripcion = inscripcionRepo.findById(id).orElseThrow();
        
        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=comprobante_" + id + ".pdf";
        response.setHeader(headerKey, headerValue);

        ComprobanteExporterPDF exporter = new ComprobanteExporterPDF(inscripcion);
        exporter.exportar(response);
    }

    @GetMapping("/mi-historial/pdf")
    public void descargarHistorialPDF(Authentication auth, HttpServletResponse response) throws IOException, DocumentException {
        Usuario usuario = usuarioRepo.findByUserName(auth.getName()).orElseThrow();
        List<Inscripcion> inscripciones = inscripcionRepo.findByUsuario(usuario);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=historial_postulaciones.pdf");

        HojaVidaExporterPDF exporter = new HojaVidaExporterPDF(inscripciones, usuario.getUserName());
        exporter.exportar(response);
    }

    private String normalizarTexto(String texto) {
        if (texto == null) return "";
        String normalized = Normalizer.normalize(texto, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("").toLowerCase();
    }

    @GetMapping("/uploads/{filename}")
    @ResponseBody
    public ResponseEntity<Resource> descargarArchivo(@PathVariable String filename) {
        try {
            Path filePath = Paths.get("uploads").resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .body(resource);
            }
        } catch (MalformedURLException e) { }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/eliminar-inscripcion/{id}")
    public String eliminarInscripcionEstudiante(@PathVariable Long id, Authentication auth) {
        String username = auth.getName();
        Usuario usuario = usuarioRepo.findByUserName(username).orElseThrow();
        Inscripcion ins = inscripcionRepo.findById(id).orElse(null);
        if (ins != null && ins.getUsuario().getId().equals(usuario.getId())) {
            inscripcionRepo.deleteById(id);
        }
        return "redirect:/convocatorias/mis-inscripciones";
    }
}
