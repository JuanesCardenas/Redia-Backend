package com.redia.back.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "action_log")
public class ActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre del usuario que hizo la acción
    private String nombreUsuario;

    // Email del usuario
    private String emailUsuario;

    // Rol del usuario
    private String rolUsuario;

    // Acción realizada
    private String accion;

    // Descripción legible
    @Column(length = 500)
    private String descripcion;

    // Detalle adicional opcional
    @Column(length = 1000)
    private String detalle;

    private LocalDateTime fecha;

    @PrePersist
    public void prePersist() {
        this.fecha = LocalDateTime.now();
        this.descripcion = buildDescripcion();
    }

    private String buildDescripcion() {
        String fechaStr = this.fecha != null
                ? this.fecha.format(DateTimeFormatter.ofPattern("dd/MM/yy"))
                : "??/??/??";
        String nombre = this.nombreUsuario != null ? this.nombreUsuario : "Desconocido";
        String rol = this.rolUsuario != null ? this.rolUsuario : "SIN_ROL";
        return fechaStr + " - " + nombre + " (" + rol + ") - " + this.accion;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getEmailUsuario() {
        return emailUsuario;
    }

    public void setEmailUsuario(String emailUsuario) {
        this.emailUsuario = emailUsuario;
    }

    public String getRolUsuario() {
        return rolUsuario;
    }

    public void setRolUsuario(String rolUsuario) {
        this.rolUsuario = rolUsuario;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}