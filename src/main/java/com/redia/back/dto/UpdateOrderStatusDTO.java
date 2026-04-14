package com.redia.back.dto;

import com.redia.back.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para actualizar el estado de un pedido.
 */
public record UpdateOrderStatusDTO(

        @NotNull(message = "El estado es requerido") OrderStatus status

) {
}