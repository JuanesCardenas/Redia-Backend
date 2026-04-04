package com.redia.back.dto;

/**
 * Respuesta al iniciar la configuración de 2FA.
 * Contiene la URL del código QR y el secret para mostrar al usuario.
 */
public record TwoFactorSetupResponseDTO(
        String qrCodeUrl,
        String secret) {
}
