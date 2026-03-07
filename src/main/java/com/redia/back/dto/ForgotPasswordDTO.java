package com.redia.back.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Record para solicitar el envío de código de verificación (olvide contraseña).
 */
public record ForgotPasswordDTO(
        @Email(message = "Debe ser un correo electrónico válido")
        @NotBlank(message = "El correo no puede estar vacío")
        String email
) {
}
