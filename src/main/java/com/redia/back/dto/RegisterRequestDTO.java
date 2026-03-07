package com.redia.back.dto;

import org.springframework.web.multipart.MultipartFile;

/**
 * DTO que representa los datos recibidos para registrar un usuario.
 */
public class RegisterRequestDTO {

    private String nombre;
    private String email;
    private String password;
    private String role;
    private MultipartFile fotoUrl;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public MultipartFile getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(MultipartFile fotoUrl) {
        this.fotoUrl = fotoUrl;
    }
}