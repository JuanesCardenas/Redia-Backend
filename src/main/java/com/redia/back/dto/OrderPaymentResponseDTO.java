package com.redia.back.dto;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para el pago de un pedido.
 */
public record OrderPaymentResponseDTO(

        String id,
        Double monto,
        String metodoPago,
        LocalDateTime fechaPago

) {
}