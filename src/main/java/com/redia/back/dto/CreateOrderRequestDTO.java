package com.redia.back.dto;

import java.util.List;

/**
 * DTO para crear un nuevo pedido desde una reserva.
 */
public record CreateOrderRequestDTO(
        String reservationId,
        String notas,
        List<OrderItemRequestDTO> items
) {
}
