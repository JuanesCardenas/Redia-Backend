package com.redia.back.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO para registrar el pago de un pedido.
 */
public record CreateOrderPaymentDTO(

        @NotNull(message = "El id del pedido es requerido") String orderId,

        @NotNull(message = "El monto es requerido") @Positive(message = "El monto debe ser mayor a 0") Double monto,

        @NotBlank(message = "El método de pago es requerido") String metodoPago

) {
}