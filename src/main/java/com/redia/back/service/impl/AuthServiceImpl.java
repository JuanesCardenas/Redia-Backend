package com.redia.back.service.impl;

import com.redia.back.dto.PasswordRecoveryRequestDTO;
import com.redia.back.dto.RegisterRequestDTO;
import com.redia.back.exception.BadRequestException;
import com.redia.back.model.Role;
import com.redia.back.model.User;
import com.redia.back.repository.UserRepository;
import com.redia.back.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio de autenticación.
 * Gestiona registro y recuperación de contraseña.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User register(RegisterRequestDTO request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("El correo ya está registrado.");
        }

        Role role;

        try {
            role = Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Rol inválido.");
        }

        User user = new User(
                request.getNombre(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                role
        );

        return userRepository.save(user);
    }

    @Override
    public void recoverPassword(PasswordRecoveryRequestDTO request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado."));

        user.setPassword(passwordEncoder.encode(request.getNuevaPassword()));

        userRepository.save(user);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado."));
    }
}