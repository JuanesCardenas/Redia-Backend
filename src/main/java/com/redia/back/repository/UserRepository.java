package com.redia.back.repository;

import com.redia.back.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad User.
 * Permite consultar usuarios en la base de datos.
 */
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Busca un usuario por email.
     * 
     * @param email correo del usuario
     * @return usuario encontrado (si existe)
     */
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByTelefono(String telefono);
}