package com.redia.back.dto;

/**
 * DTO para representar un plato dentro de la respuesta de un pedido.
 */
public record OrderDishDTO(
        String id,
        String dishNombre,
        int cantidad,
        Double precioUnitario,
        Double subtotal,
        String notas
) {
}
