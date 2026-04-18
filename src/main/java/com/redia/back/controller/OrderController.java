package com.redia.back.controller;

import com.redia.back.dto.*;
import com.redia.back.service.OrderService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de pedidos.
 * Flujo: CREATED → IN_PROGRESS → READY → PAID
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // ─────────────── MESERO ───────────────

    /**
     * Crear un nuevo pedido desde una reserva confirmada.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('MESERO')")
    public ResponseEntity<OrderResponseDTO> crearPedido(@RequestBody CreateOrderRequestDTO request) {
        logger.info("POST /api/orders - crear pedido");
        return ResponseEntity.ok(orderService.crearPedido(request));
    }

    /**
     * Ver pedidos activos del mesero autenticado.
     */
    @GetMapping("/my")
    @PreAuthorize("hasAuthority('MESERO')")
    public ResponseEntity<List<OrderResponseDTO>> misPedidos() {
        return ResponseEntity.ok(orderService.obtenerPedidosMesero());
    }

    /**
     * Enviar pedido a cocina (CREATED → IN_PROGRESS).
     */
    @PutMapping("/{id}/send-to-kitchen")
    @PreAuthorize("hasAuthority('MESERO')")
    public ResponseEntity<OrderResponseDTO> enviarACocina(@PathVariable Long id) {
        logger.info("PUT /api/orders/{}/send-to-kitchen", id);
        return ResponseEntity.ok(orderService.enviarACocina(id));
    }

    /**
     * Obtener reservas CONFIRMADAS disponibles para crear un pedido.
     */
    @GetMapping("/reservations/active")
    @PreAuthorize("hasAuthority('MESERO')")
    public ResponseEntity<List<ReservationResponseDTO>> reservasActivas() {
        return ResponseEntity.ok(orderService.obtenerReservasActivas());
    }

    // ─────────────── COCINERO ───────────────

    /**
     * Ver cola de pedidos en cocina (CREATED + IN_PROGRESS).
     */
    @GetMapping("/kitchen")
    @PreAuthorize("hasAuthority('COCINERO')")
    public ResponseEntity<List<OrderResponseDTO>> pedidosCocina() {
        return ResponseEntity.ok(orderService.obtenerPedidosCocina());
    }

    /**
     * Marcar pedido como listo (IN_PROGRESS → READY).
     */
    @PutMapping("/{id}/mark-ready")
    @PreAuthorize("hasAuthority('COCINERO')")
    public ResponseEntity<OrderResponseDTO> marcarListo(@PathVariable Long id) {
        logger.info("PUT /api/orders/{}/mark-ready", id);
        return ResponseEntity.ok(orderService.marcarListo(id));
    }

    // ─────────────── CAJERO ───────────────

    /**
     * Ver pedidos listos para cobrar (READY).
     */
    @GetMapping("/cashier")
    @PreAuthorize("hasAuthority('CAJERO')")
    public ResponseEntity<List<OrderResponseDTO>> pedidosCajero() {
        return ResponseEntity.ok(orderService.obtenerPedidosCajero());
    }

    /**
     * Registrar pago del pedido (READY → PAID).
     */
    @PutMapping("/{id}/pay")
    @PreAuthorize("hasAuthority('CAJERO')")
    public ResponseEntity<OrderResponseDTO> registrarPago(
            @PathVariable Long id,
            @RequestBody PayOrderRequestDTO request) {
        logger.info("PUT /api/orders/{}/pay", id);
        return ResponseEntity.ok(orderService.registrarPago(id, request));
    }

    // ─────────────── COMPARTIDO ───────────────

    /**
     * Detalle de un pedido (cualquier rol del personal).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('MESERO','COCINERO','CAJERO','ADMINISTRADOR')")
    public ResponseEntity<OrderResponseDTO> detallePedido(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.obtenerPedido(id));
    }
}
