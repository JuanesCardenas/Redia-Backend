package com.redia.back.dto;

/**
 * DTO para recibir credenciales de inicio de sesión.
 */
public record LoginRequestDTO(
        String email,
        String password) {
}