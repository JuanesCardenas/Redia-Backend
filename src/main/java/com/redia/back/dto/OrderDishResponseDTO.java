package com.redia.back.dto;

/**
 * DTO de respuesta para los platos de un pedido.
 */
public record OrderDishResponseDTO(

        String dishId,
        String nombre,
        int cantidad,
        Double precioUnitario,
        Double subtotal

) {
}