package com.redia.back.service.impl;

import com.redia.back.dto.UserResponseDTO;
import com.redia.back.exception.BadRequestException;
import com.redia.back.model.User;
import com.redia.back.repository.UserRepository;
import com.redia.back.service.UserAdminService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación del servicio administrativo de usuarios.
 */
@Service
@Transactional
public class UserAdminServiceImpl implements UserAdminService {

        private final UserRepository userRepository;

        public UserAdminServiceImpl(UserRepository userRepository) {
                this.userRepository = userRepository;
        }

        /**
         * Obtiene todos los usuarios registrados en el sistema
         * y los transforma a DTO para evitar exponer datos sensibles.
         */
        @Override
        public List<UserResponseDTO> getAllUsers() {

                return userRepository.findAll()
                                .stream()
                                .map(user -> new UserResponseDTO(
                                                user.getId(),
                                                user.getNombre(),
                                                user.getEmail(),
                                                user.getTelefono(),
                                                user.getRole(),
                                                user.getFotoUrl()))
                                .toList();
        }

        /**
         * Elimina un usuario por su id.
         */
        @Override
        public void deleteUser(String userId) {

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new BadRequestException("Usuario no encontrado."));

                userRepository.delete(user);
        }
}