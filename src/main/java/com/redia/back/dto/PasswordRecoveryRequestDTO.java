package com.redia.back.dto;

/**
 * DTO para solicitar recuperación de contraseña.
 */
public class PasswordRecoveryRequestDTO {

    private String email;
    private String nuevaPassword;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNuevaPassword() {
        return nuevaPassword;
    }

    public void setNuevaPassword(String nuevaPassword) {
        this.nuevaPassword = nuevaPassword;
    }
}