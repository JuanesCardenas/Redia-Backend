package com.redia.back.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Record para resetear la contraseña usando código de verificación.
 */
public record ResetPasswordDTO(
                @Email(message = "Debe ser un correo electrónico válido") @NotBlank(message = "El correo no puede estar vacío") String email,

                @NotBlank(message = "El código de verificación no puede estar vacío") String verificationCode,

                @NotBlank(message = "La nueva contraseña no puede estar vacía") @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres") @Pattern(regexp = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).*$", message = "La contraseña debe contener al menos una letra mayúscula, una letra minúscula, un número y un carácter especial") String newPassword) {
}
