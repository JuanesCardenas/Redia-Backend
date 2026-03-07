package com.redia.back.dto;

import java.time.LocalDateTime;

/**
 * DTO que representa la solicitud de creación de una reserva.
 */
public record CreateReservationRequestDTO(

        /**
         * Fecha y hora solicitada para la reserva
         */
        LocalDateTime fechaReserva,

        /**
         * Número de personas
         */
        int numeroPersonas

) {
}