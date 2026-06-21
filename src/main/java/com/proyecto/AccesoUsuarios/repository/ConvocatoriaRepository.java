package com.proyecto.AccesoUsuarios.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository; // Buena práctica añadir esto

import com.proyecto.AccesoUsuarios.model.Convocatoria;
import com.proyecto.AccesoUsuarios.model.Usuario;

@Repository
public interface ConvocatoriaRepository extends JpaRepository<Convocatoria, Long> {
    
    // 1. Para el estudiante: Ver solo las que dicen "ACTIVA"
    List<Convocatoria> findByEstado(String estado);

    // 2. Para el Dashboard Institución: Contar cuántas han creado (KPI)
    long countByCreador(Usuario creador);

    // 3. ¡ESTE FALTABA! Para la lista "Mis Convocatorias"
    // Busca todas las convocatorias creadas por una institución específica
    List<Convocatoria> findByCreador(Usuario creador);

    // Para buscar una específica y asegurarnos que le pertenece a quien la quiere editar
    Optional<Convocatoria> findByIdAndCreador(Long id, Usuario creador);
    
    // 4. (Opcional) Si tu Dashboard Admin usa este conteo general
    long countByActivaTrue();

    // Busca convocatorias APROBADAS que coincidan con el texto ingresado
    @Query("SELECT c FROM Convocatoria c WHERE c.estado = 'APROBADA' AND " +
           "(LOWER(c.titulo) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.descripcion) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Convocatoria> buscarPorKeyword(@Param("keyword") String keyword);

    // Lista solo las que están marcadas como activas para el estudiante
    List<Convocatoria> findByActivaTrue();

    List<Convocatoria> findTop3ByEstadoOrderByIdDesc(String estado);

    @Query("SELECT c FROM Convocatoria c WHERE c.estado = 'APROBADA' AND " +
           "(:keyword IS NULL OR LOWER(c.titulo) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:categoria IS NULL OR c.categoria = :categoria OR :categoria = '') AND " +
           "(:area IS NULL OR c.areaConocimiento = :area OR :area = '') AND " +
           "(:modalidad IS NULL OR c.modalidad = :modalidad OR :modalidad = '')")
    List<Convocatoria> buscarConFiltrosAvanzados(
        @Param("keyword") String keyword,
        @Param("categoria") String categoria,
        @Param("area") String area,
        @Param("modalidad") String modalidad
    );

    @Query("SELECT c FROM Convocatoria c WHERE c.creador = :creador AND c.fechaFin >= CURRENT_DATE AND c.fechaFin <= :limite ORDER BY c.fechaFin ASC")
    List<Convocatoria> findProximasACerrar(@Param("creador") Usuario creador, @Param("limite") java.time.LocalDate limite);
}