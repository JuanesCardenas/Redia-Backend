package com.redia.back.repository;

import com.redia.back.model.Order;
import com.redia.back.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio de acceso a datos para la entidad Order.
 * Permite gestionar los pedidos del sistema.
 */
public interface OrderRepository extends JpaRepository<Order, String> {

    /**
     * Busca pedidos por estado
     * 
     * @param status estado del pedido
     * @return lista de pedidos
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Verifica si existe un pedido para una reserva específica.
     *
     * @param reservationId ID de la reserva
     * @return true si existe un pedido para la reserva, false en caso contrario
     */
    boolean existsByReservationId(String reservationId);
}