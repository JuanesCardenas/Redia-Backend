package com.redia.back.dto;

/**
 * DTO para solicitar recuperación de contraseña.
 */
public record PasswordRecoveryRequestDTO(
        String email,
        String nuevaPassword) {
}