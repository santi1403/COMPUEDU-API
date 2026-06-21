package com.proyecto.AccesoUsuarios.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

import com.proyecto.AccesoUsuarios.model.Convocatoria;
import com.proyecto.AccesoUsuarios.repository.ConvocatoriaRepository;

@Controller
@RequestMapping("/admin/gestion-convocatorias")
public class AdminConvocatoriaController {

    @Autowired
    private ConvocatoriaRepository convocatoriaRepo;

    // 1. Ver todas las convocatorias (De todas las instituciones)
    @GetMapping
    public String listarTodas(Model model) {
        model.addAttribute("convocatorias", convocatoriaRepo.findAll());
        return "admin/gestion_convocatorias"; // Vista nueva
    }

    // 2. Acción de ANULAR
    @GetMapping("/anular/{id}")
    public String anularConvocatoria(@PathVariable Long id, RedirectAttributes attrs) {
        Convocatoria conv = convocatoriaRepo.findById(id).orElseThrow();
        
        conv.setEstado("ANULADA"); // Cambiamos el estado
        conv.setActiva(false);     // También bajamos el flag de activa por seguridad
        
        convocatoriaRepo.save(conv);
        
        attrs.addFlashAttribute("mensaje", "La convocatoria ha sido anulada correctamente.");
        return "redirect:/admin/gestion-convocatorias";
    }

    // 3. Acción de REACTIVAR (Por si el admin se equivocó)
    @GetMapping("/reactivar/{id}")
    public String reactivarConvocatoria(@PathVariable Long id, RedirectAttributes attrs) {
        Convocatoria conv = convocatoriaRepo.findById(id).orElseThrow();
        
        conv.setEstado("ACTIVA");
        conv.setActiva(true);
        
        convocatoriaRepo.save(conv);
        
        attrs.addFlashAttribute("mensaje", "La convocatoria ha sido reactivada.");
        return "redirect:/admin/gestion-convocatorias";
    }

    @PostMapping("/validar/{id}")
    @ResponseBody
    public Map<String, Object> validarConvocatoria(@PathVariable Long id) {
        Map<String, Object> resp = new HashMap<>();
        Convocatoria conv = convocatoriaRepo.findById(id).orElse(null);
        if (conv == null) { resp.put("success", false); return resp; }
        conv.setEstado("APROBADA");
        conv.setActiva(true);
        convocatoriaRepo.save(conv);
        resp.put("success", true);
        return resp;
    }

    @PostMapping("/rechazar/{id}")
    @ResponseBody
    public Map<String, Object> rechazarConvocatoria(@PathVariable Long id) {
        Map<String, Object> resp = new HashMap<>();
        Convocatoria conv = convocatoriaRepo.findById(id).orElse(null);
        if (conv == null) { resp.put("success", false); return resp; }
        conv.setEstado("RECHAZADA");
        conv.setActiva(false);
        convocatoriaRepo.save(conv);
        resp.put("success", true);
        return resp;
    }
}