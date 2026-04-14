package com.redia.back.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * DTO para crear un pedido.
 * Contiene la lista de platos solicitados.
 */
public record CreateOrderRequestDTO(

        @NotNull(message = "El id de la reserva es requerido") String reservationId,

        @NotEmpty(message = "El pedido debe tener al menos un plato") List<CreateOrderDishDTO> dishes

) {
}