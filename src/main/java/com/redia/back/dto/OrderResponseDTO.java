package com.redia.back.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta con la información completa de un pedido.
 */
public record OrderResponseDTO(
        String id,
        String status,
        String reservationId,
        String clienteNombre,
        String clienteEmail,
        String meseroNombre,
        LocalDateTime fechaCreacion,
        Double total,
        String notas,
        List<OrderDishDTO> dishes
) {
}
