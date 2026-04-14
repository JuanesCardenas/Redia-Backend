package com.redia.back.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta para un pedido.
 */
public record OrderResponseDTO(

        String id,
        String reservationId,
        LocalDateTime fechaCreacion,
        String status,
        Double total,
        List<OrderDishResponseDTO> dishes

) {
}