package com.redia.back.repository;

import com.redia.back.model.Dish;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio de acceso a datos para la entidad Dish.
 * Permite consultar los platos del sistema.
 */
public interface DishRepository extends JpaRepository<Dish, String> {

    /**
     * Obtiene los platos disponibles
     * 
     * @return lista de platos disponibles
     */
    List<Dish> findByAvailableTrue();

    /**
     * Busca platos por categoría
     * 
     * @param categoria categoría del plato
     * @return lista de platos encontrados
     */
    List<Dish> findByCategoria(String categoria);
}