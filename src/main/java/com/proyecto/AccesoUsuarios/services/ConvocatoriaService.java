package com.proyecto.AccesoUsuarios.services;

import com.proyecto.AccesoUsuarios.model.Convocatoria;
import com.proyecto.AccesoUsuarios.repository.ConvocatoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class ConvocatoriaService {

    private final ConvocatoriaRepository convocatoriaRepository;

    public ConvocatoriaService(ConvocatoriaRepository convocatoriaRepository) {
        this.convocatoriaRepository = convocatoriaRepository;
    }

    public List<Convocatoria> listarParaEstudiante(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return convocatoriaRepository.buscarPorKeyword(keyword);
        }
        return convocatoriaRepository.findByEstado("APROBADA");
    }

    public Map<String, Object> obtenerEstadisticasPython(Long institucionId) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:5000/api/stats/institucion/" + institucionId;
        return restTemplate.getForObject(url, Map.class);
    }
}