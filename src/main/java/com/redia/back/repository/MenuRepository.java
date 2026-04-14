package com.redia.back.repository;

import com.redia.back.model.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad Menu.
 * Permite gestionar los menús del sistema.
 */
public interface MenuRepository extends JpaRepository<Menu, String> {

    /**
     * Obtiene el menú activo
     * 
     * @return menú activo (si existe)
     */
    Optional<Menu> findByActivoTrue();
}