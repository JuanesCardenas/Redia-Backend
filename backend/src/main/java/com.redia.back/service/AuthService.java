package com.redia.back.service;

import com.redia.back.dto.RegisterRequestDTO;
import com.redia.back.model.User;

/**
 * Servicio que define las operaciones de autenticación.
 */
public interface AuthService {

    /**
     * Registra un nuevo usuario en el sistema.
     * @param request datos del usuario
     * @return usuario creado
     */
    User register(RegisterRequestDTO request);
}