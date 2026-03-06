package com.redia.back.controller;

import com.redia.back.dto.CreateReservationRequestDTO;
import com.redia.back.dto.ReservationResponseDTO;
import com.redia.back.service.ReservationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de reservas.
 */
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private static final Logger logger = LoggerFactory.getLogger(ReservationController.class);

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    /**
     * Crear una reserva.
     */
    @PostMapping
    public ResponseEntity<ReservationResponseDTO> crearReserva(
            @RequestBody CreateReservationRequestDTO request) {

        logger.info("Endpoint crear reserva");

        ReservationResponseDTO reserva =
                reservationService.crearReserva(request, null);

        return ResponseEntity.ok(reserva);
    }

    /**
     * Ver reservas del cliente autenticado.
     */
    @GetMapping("/my")
    public ResponseEntity<List<ReservationResponseDTO>> misReservas() {

        return ResponseEntity.ok(
                reservationService.obtenerReservasCliente(null)
        );
    }

    /**
     * Ver todas las reservas (admin/recepcionista).
     */
    @GetMapping
    public ResponseEntity<List<ReservationResponseDTO>> todas() {

        return ResponseEntity.ok(
                reservationService.obtenerTodas()
        );
    }

    /**
     * Confirmar reserva.
     */
    @PutMapping("/confirm/{id}")
    public ResponseEntity<String> confirmar(@PathVariable String id) {

        reservationService.confirmarReserva(id);

        return ResponseEntity.ok("Reserva confirmada");
    }

    /**
     * Rechazar reserva.
     */
    @PutMapping("/reject/{id}")
    public ResponseEntity<String> rechazar(@PathVariable String id) {

        reservationService.rechazarReserva(id);

        return ResponseEntity.ok("Reserva rechazada");
    }

    /**
     * Cancelar reserva.
     */
    @PutMapping("/cancel/{id}")
    public ResponseEntity<String> cancelar(@PathVariable String id) {

        reservationService.cancelarReserva(id);

        return ResponseEntity.ok("Reserva cancelada");
    }

    /**
     * Finalizar reserva.
     */
    @PutMapping("/finish/{id}")
    public ResponseEntity<String> finalizar(@PathVariable String id) {

        reservationService.finalizarReserva(id);

        return ResponseEntity.ok("Reserva finalizada");
    }
}