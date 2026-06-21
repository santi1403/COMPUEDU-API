package com.proyecto.AccesoUsuarios.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "usuarios")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String userName;

    // Ahora obligatorios
    @Column(nullable = false)
    private String nombre;

    @Column(nullable = true)
    private String apellido;

    @Column(nullable = false)
    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @Column(nullable = false)
    private String rol; 

    @Column(name="habilitado", nullable = false)
    private boolean habilitado = true;

    @Column(name = "estado_cuenta")
    private String estadoCuenta = "ACTIVO";

    // Ahora obligatorio
    @Column(name = "email", nullable = false)
    private String email;

    // Ahora obligatorio
    @Column(name = "documento", nullable = true)
    private String documento;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "nit")
    private String nit;

    @Column(name = "nombre_institucion")
    private String nombreInstitucion;

    @Column(name = "descripcion_institucion", length = 1000)
    private String descripcionInstitucion;

    // Este se mantiene opcional (nullable = true por defecto)
    @Column(name = "token_recuperacion")
    private String tokenRecuperacion;

    // Método útil para la interfaz administrativa
    public String getEstadoTexto() {
        return this.habilitado ? "Activo" : "Inactivo";
    }
}