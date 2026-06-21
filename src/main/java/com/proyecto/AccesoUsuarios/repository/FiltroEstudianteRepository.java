package com.proyecto.AccesoUsuarios.repository;

import com.proyecto.AccesoUsuarios.model.FiltroEstudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FiltroEstudianteRepository extends JpaRepository<FiltroEstudiante, Long> {
}