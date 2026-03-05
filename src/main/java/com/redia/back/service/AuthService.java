package com.redia.back.service;

import com.redia.back.dto.PasswordRecoveryRequestDTO;
import com.redia.back.dto.RegisterRequestDTO;
import com.redia.back.model.User;

/**
 * Servicio que define las operaciones relacionadas con autenticación.
 */
public interface AuthService {

    User register(RegisterRequestDTO request);

    void recoverPassword(PasswordRecoveryRequestDTO request);

    User findByEmail(String email);
}