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

    @Column(nullable = false, unique = true)
    private String telefono;

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

    /**
     * Código de verificación para recuperación de contraseña
     */
    private String codigoVerificacion;

    /**
     * Fecha de expiración del código de verificación
     */
    private LocalDateTime codigoExpiraEn;

    /**
     * URL de la foto de perfil del usuario almacenada en Cloudinary
     */
    private String fotoUrl;

    /**
     * Clave secreta TOTP para la verificación de dos pasos
     */
    private String twoFactorSecret;

    /**
     * Indica si el usuario tiene habilitada la verificación de dos pasos
     */
    private boolean twoFactorEnabled = false;

    /**
     * Indica si el usuario ha solicitado la eliminación de su cuenta
     */
    private boolean bajaSolicitada = false;

    public User() {
    }

    public User(String nombre, String email, String telefono, String password, Role role) {
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
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

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
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

    public String getCodigoVerificacion() {
        return codigoVerificacion;
    }

    public void setCodigoVerificacion(String codigoVerificacion) {
        this.codigoVerificacion = codigoVerificacion;
    }

    public LocalDateTime getCodigoExpiraEn() {
        return codigoExpiraEn;
    }

    public void setCodigoExpiraEn(LocalDateTime codigoExpiraEn) {
        this.codigoExpiraEn = codigoExpiraEn;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public String getTwoFactorSecret() {
        return twoFactorSecret;
    }

    public void setTwoFactorSecret(String twoFactorSecret) {
        this.twoFactorSecret = twoFactorSecret;
    }

    public boolean isTwoFactorEnabled() {
        return twoFactorEnabled;
    }

    public void setTwoFactorEnabled(boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
    }

    public boolean isBajaSolicitada() {
        return bajaSolicitada;
    }

    public void setBajaSolicitada(boolean bajaSolicitada) {
        this.bajaSolicitada = bajaSolicitada;
    }
}