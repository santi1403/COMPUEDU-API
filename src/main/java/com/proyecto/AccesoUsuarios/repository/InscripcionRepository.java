package com.proyecto.AccesoUsuarios.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.AccesoUsuarios.model.Convocatoria;
import com.proyecto.AccesoUsuarios.model.Inscripcion; // Importante
import com.proyecto.AccesoUsuarios.model.Usuario; // Importante

@Repository
// ... imports

public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {
    
    // Métodos existentes...
    List<Inscripcion> findByUsuario(Usuario usuario);
    boolean existsByUsuarioAndConvocatoria(Usuario usuario, Convocatoria convocatoria);

    // MÉTODOS MÁGICOS DE JPA (Sin @Query)
    // Spring entiende esto como: "Cuenta inscripciones donde la convocatoria tenga un creador igual a X"
    long countByConvocatoria_Creador(Usuario creador);

    // Spring entiende esto como: "Busca inscripciones donde la convocatoria tenga un creador igual a X"
    List<Inscripcion> findByConvocatoria_Creador(Usuario creador);

    long countByConvocatoria_CreadorAndEstado(Usuario creador, String estado);

    long countByEstado(String estado);

    List<Inscripcion> findTop6ByOrderByIdDesc();

    long countByConvocatoria(Convocatoria convocatoria);
}