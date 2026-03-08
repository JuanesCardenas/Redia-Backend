package com.redia.back.controller;

import com.redia.back.dto.ConfirmReservationRequestDTO;
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

        ReservationResponseDTO reserva = reservationService.crearReserva(request);

        return ResponseEntity.ok(reserva);
    }

    /**
     * Ver reservas del cliente autenticado.
     */
    @GetMapping("/my")
    public ResponseEntity<List<ReservationResponseDTO>> misReservas() {

        return ResponseEntity.ok(
                reservationService.obtenerReservasCliente());
    }

    /**
     * Ver todas las reservas (admin/recepcionista).
     */
    @GetMapping
    public ResponseEntity<List<ReservationResponseDTO>> todas() {

        return ResponseEntity.ok(
                reservationService.obtenerTodas());
    }

    /**
     * El recepcionista asigna mesas y confirma la reserva.
     */
    @PutMapping("/confirm-with-tables/{id}")
    public ResponseEntity<String> confirmarConMesas(
            @PathVariable String id,
            @RequestBody ConfirmReservationRequestDTO request) {

        reservationService.assignTablesAndConfirmReservation(id, request);

        return ResponseEntity.ok("Reserva confirmada y mesas asignadas");
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