package com.proyecto.AccesoUsuarios.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "convocatorias")
public class Convocatoria {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String titulo;

    @Column(columnDefinition = "TEXT") 
    private String descripcion;

    private String categoria;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private boolean activa = true;
    private String estado = "APROBADA";

    private String areaConocimiento;
    private String modalidad;

    private String tipoApoyo;
    private String precioSemestre;

    // RELACIÓN NECESARIA PARA EL DASHBOARD
    @ManyToOne
    @JoinColumn(name = "creador_id") 
    private Usuario creador;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    
    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Usuario getCreador() { return creador; }
    public void setCreador(Usuario creador) { this.creador = creador; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getAreaConocimiento() { return areaConocimiento; }
    public void setAreaConocimiento(String areaConocimiento) { this.areaConocimiento = areaConocimiento; }

    public String getModalidad() { return modalidad; }
    public void setModalidad(String modalidad) { this.modalidad = modalidad; }

    public String getTipoApoyo() { return tipoApoyo; }
    public void setTipoApoyo(String tipoApoyo) { this.tipoApoyo = tipoApoyo; }

    public String getPrecioSemestre() { return precioSemestre; }
    public void setPrecioSemestre(String precioSemestre) { this.precioSemestre = precioSemestre; }
}