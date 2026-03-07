package com.redia.back.dto;

import org.springframework.web.multipart.MultipartFile;

/**
 * DTO que representa los datos recibidos para registrar un usuario.
 */
public record RegisterRequestDTO(

        String nombre,
        String email,
        String telefono,
        String password,
        String role,
        MultipartFile fotoUrl

) {
}