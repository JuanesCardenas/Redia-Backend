package com.redia.back.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa un usuario del sistema del restaurante.
 * Contiene información básica para autenticación y autorización.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Contraseña encriptada con BCrypt.
     */
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private boolean activo = true;

    private LocalDateTime fechaCreacion = LocalDateTime.now();

    public User() {}

    public User(String nombre, String email, String password, Role role) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // Getters y Setters

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    /**
     * Se espera que la contraseña llegue ya encriptada desde el servicio.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
}