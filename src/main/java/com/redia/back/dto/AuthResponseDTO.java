package com.redia.back.dto;

/**
 * Respuesta enviada tras autenticación o refresh.
 */
public record AuthResponseDTO(
        String accessToken,
        String refreshToken,
        String email,
        String role) {
}