package com.redia.back.dto;

/**
 * Request para verificar el código TOTP durante el login o al activar 2FA.
 */
public record TwoFactorVerifyRequestDTO(
        String email,
        int code) {
}
