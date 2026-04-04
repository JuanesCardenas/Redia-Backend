package com.redia.back.dto;

/**
 * Respuesta enviada tras autenticación o refresh.
 * requiresTwoFactor=true indica que se necesita verificar el código TOTP antes de otorgar acceso.
 */
public record AuthResponseDTO(
                String accessToken,
                String refreshToken,
                String email,
                String role,
                String nombre,
                String telefono,
                String fotoUrl,
                boolean requiresTwoFactor) {

    /** Constructor de conveniencia para respuestas sin 2FA (flujo normal) */
    public AuthResponseDTO(String accessToken, String refreshToken, String email,
                           String role, String nombre, String telefono, String fotoUrl) {
        this(accessToken, refreshToken, email, role, nombre, telefono, fotoUrl, false);
    }
}