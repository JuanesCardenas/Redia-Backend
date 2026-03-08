package com.redia.back.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para recibir credenciales de inicio de sesión.
 */
public record LoginRequestDTO(
                @NotBlank(message = "El correo es requerido") @Email(message = "El correo debe ser válido") String email,

                @NotBlank(message = "La contraseña es requerida") String password,

                @NotBlank(message = "El reCAPTCHA es requerido") String recaptchaToken

) {
}