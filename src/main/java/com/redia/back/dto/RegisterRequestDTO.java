package com.redia.back.dto;

import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO que representa los datos recibidos para registrar un usuario.
 */
public record RegisterRequestDTO(

                @NotBlank(message = "El nombre es requerido") String nombre,

                @NotBlank(message = "El correo es requerido") @Email(message = "El correo electrónico debe ser válido") String email,

                @NotBlank(message = "El teléfono es requerido") @Pattern(regexp = "^\\d{10}$", message = "El teléfono debe tener exactamente 10 números") String telefono,

                @NotBlank(message = "La contraseña es requerida") @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres") @Pattern(regexp = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).*$", message = "La contraseña debe contener al menos una letra mayúscula, una letra minúscula, un número y un carácter especial") String password,

                @NotBlank(message = "El rol es requerido") String role,

                MultipartFile fotoUrl,

                @NotBlank(message = "El reCAPTCHA es requerido") String recaptchaToken

) {
}