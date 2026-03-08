package com.redia.back.service;

import com.redia.back.dto.UserResponseDTO;
import java.util.List;

/**
 * Servicio que define las operaciones administrativas sobre usuarios.
 */
public interface UserAdminService {

    /**
     * Obtiene todos los usuarios del sistema.
     */
    List<UserResponseDTO> getAllUsers();

    /**
     * Elimina un usuario por su identificador.
     *
     */
    void deleteUser(String userId);
}