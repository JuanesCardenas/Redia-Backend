package com.redia.back.repository;

import com.redia.back.model.OrderDishes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio de acceso a datos para la entidad OrderDishes.
 * Permite consultar los platos dentro de los pedidos.
 */
public interface OrderDishesRepository extends JpaRepository<OrderDishes, String> {

    /**
     * Obtiene los platos de un pedido
     * 
     * @param orderId id del pedido
     * @return lista de platos del pedido
     */
    List<OrderDishes> findByOrderId(String orderId);
}