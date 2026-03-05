package com.redia.back.dto;

/**
 * Respuesta enviada tras autenticación o refresh.
 */
public class AuthResponseDTO {

    private String accessToken;
    private String refreshToken;
    private String email;
    private String role;

    public AuthResponseDTO(String accessToken, String refreshToken, String email, String role) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.email = email;
        this.role = role;
    }

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}