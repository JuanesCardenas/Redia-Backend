package com.redia.back.repository;

import com.redia.back.model.DishIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio de acceso a datos para la relación DishIngredient.
 * Permite consultar los ingredientes asociados a los platos.
 */
public interface DishIngredientRepository extends JpaRepository<DishIngredient, String> {

    /**
     * Obtiene los ingredientes de un plato
     * 
     * @param dishId id del plato
     * @return lista de relaciones DishIngredient
     */
    List<DishIngredient> findByDishId(String dishId);

    /**
     * Obtiene las relaciones por ingrediente
     * 
     * @param ingredientId id del ingrediente
     * @return lista de relaciones DishIngredient
     */
    List<DishIngredient> findByIngredientId(String ingredientId);
}