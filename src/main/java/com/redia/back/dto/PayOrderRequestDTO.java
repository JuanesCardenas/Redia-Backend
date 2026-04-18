package com.redia.back.dto;

/**
 * DTO para registrar el pago de un pedido por parte del cajero.
 */
public record PayOrderRequestDTO(
        String metodoPago
) {
}
