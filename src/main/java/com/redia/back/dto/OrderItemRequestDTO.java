package com.redia.back.dto;

/**
 * DTO para representar un ítem al crear un pedido.
 */
public record OrderItemRequestDTO(
        Long dishId,
        int cantidad,
        String notas
) {
}
