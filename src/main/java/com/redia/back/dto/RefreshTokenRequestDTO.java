package com.redia.back.dto;

/**
 * DTO para solicitar nuevo access token usando refresh token.
 */
public record RefreshTokenRequestDTO(
        String refreshToken) {
}