package com.redia.back.repository;

import com.redia.back.model.Order;
import com.redia.back.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, String> {

    /** Pedidos filtrados por estado — usado por cocinero y cajero */
    List<Order> findByStatusIn(List<OrderStatus> statuses);

    /** Pedidos del mesero autenticado */
    List<Order> findByMeseroIdOrderByFechaCreacionDesc(String meseroId);

    /** Todos los pedidos activos (no pagados, no cancelados) */
    List<Order> findByStatusNotIn(List<OrderStatus> statuses);

    /** Verificar si ya existe un pedido activo para una reserva */
    boolean existsByReservationIdAndStatusNotIn(String reservationId, List<OrderStatus> excludedStatuses);
}
