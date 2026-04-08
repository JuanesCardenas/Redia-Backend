package com.redia.back.controller;

import com.redia.back.dto.CreateReservationRequestDTO;
import com.redia.back.dto.ReservationResponseDTO;
import com.redia.back.model.DinningTable;
import com.redia.back.service.DinningTableService;
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
    private final DinningTableService dinningTableService;

    public ReservationController(ReservationService reservationService, DinningTableService dinningTableService) {
        this.reservationService = reservationService;
        this.dinningTableService = dinningTableService;
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
     * Ver mesas disponibles por fecha y rango de horas.
     */
    @GetMapping("/mesas-disponibles")
    public ResponseEntity<List<com.redia.back.dto.TableAvailabilityDTO>> getMesasDisponibles(
            @RequestParam("inicio") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime inicio,
            @RequestParam("fin") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime fin) {
        
        return ResponseEntity.ok(reservationService.getMesasDisponibles(inicio, fin));
    }

    /**
     * Obtener todas las mesas registradas (para recepcionista: asignación en
     * confirmación).
     */
    @GetMapping("/tables")
    public ResponseEntity<List<DinningTable>> getMesas() {
        return ResponseEntity.ok(dinningTableService.getAllDinningTables());
    }

    /**
     * Cancelar una reserva sin restricción de tiempo (uso del recepcionista).
     */
    @PutMapping("/force-cancel/{id}")
    public ResponseEntity<String> cancelarForzado(@PathVariable String id) {

        reservationService.cancelarReservaForzado(id);

        return ResponseEntity.ok("Reserva cancelada por el recepcionista");
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