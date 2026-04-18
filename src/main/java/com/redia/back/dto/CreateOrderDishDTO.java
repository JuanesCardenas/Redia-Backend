package com.redia.back.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para representar un plato dentro de un pedido.
 */
public record CreateOrderDishDTO(

        @NotNull(message = "El id del plato es requerido") String dishId,

        @Min(value = 1, message = "La cantidad debe ser al menos 1") int cantidad

) {
}