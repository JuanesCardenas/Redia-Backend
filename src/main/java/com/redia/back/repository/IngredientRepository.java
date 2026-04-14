package com.redia.back.repository;

import com.redia.back.model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio de acceso a datos para la entidad Ingredient.
 * Permite gestionar los ingredientes del inventario.
 */
public interface IngredientRepository extends JpaRepository<Ingredient, String> {

    /**
     * Busca ingredientes por categoría
     * 
     * @param categoria categoría del ingrediente
     * @return lista de ingredientes
     */
    List<Ingredient> findByCategoria(String categoria);

    /**
     * Verifica si existe un ingrediente por nombre
     * 
     * @param nombre nombre del ingrediente
     * @return true si existe
     */
    boolean existsByNombre(String nombre);
}