package com.redia.back.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Record para cambiar la contraseña de un usuario autenticado.
 */
public record ChangePasswordDTO(
        @NotBlank(message = "La contraseña actual no puede estar vacía")
        String oldPassword,

        @NotBlank(message = "La nueva contraseña no puede estar vacía")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z]).*$",
                message = "La contraseña debe contener al menos una letra mayúscula, una letra minúscula y un número"
        )
        String newPassword
) {
}
