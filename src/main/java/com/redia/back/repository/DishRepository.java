package com.redia.back.repository;

import com.redia.back.model.Dish;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DishRepository extends JpaRepository<Dish, String> {

    /** Obtener todos los platos disponibles del menú */
    List<Dish> findByAvailableTrue();
}
