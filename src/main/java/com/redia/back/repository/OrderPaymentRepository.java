package com.redia.back.repository;

import com.redia.back.model.OrderPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad OrderPayment.
 * Permite gestionar los pagos de los pedidos.
 */
public interface OrderPaymentRepository extends JpaRepository<OrderPayment, String> {

    /**
     * Busca el pago asociado a un pedido
     * 
     * @param orderId id del pedido
     * @return pago del pedido (si existe)
     */
    Optional<OrderPayment> findByOrderId(String orderId);
}