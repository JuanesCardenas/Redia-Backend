package com.redia.back.dto;

import com.redia.back.model.Role;

/**
 * DTO que representa la información pública de un usuario.
 * Se utiliza para devolver datos de usuarios sin exponer información sensible.
 */
public record UserResponseDTO(

        String id,
        String nombre,
        String email,
        String telefono,
        Role role,
        String fotoUrl,
        boolean bajaSolicitada

) {

}
