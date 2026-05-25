package com.redia.back.controller;

import com.redia.back.dto.*;
import com.redia.back.model.PaymentMethod;
import com.redia.back.service.OrderService;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
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
    private final MeterRegistry meterRegistry;

    public OrderController(OrderService orderService, MeterRegistry meterRegistry) {
        this.orderService = orderService;
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    void registerOrderMetrics() {
        meterRegistry.counter("redia.orders.created", "result", "success");

        for (PaymentMethod method : PaymentMethod.values()) {
            meterRegistry.counter("redia.order.payments", "result", "success", "method", method.name());
            Timer.builder("redia.order.payment.duration")
                    .description("Tiempo que tarda el sistema en registrar el pago de un pedido listo")
                    .tag("result", "success")
                    .tag("method", method.name())
                    .register(meterRegistry);
        }
    }

    // ─────────────── MESERO ───────────────

    /**
     * Crear un nuevo pedido desde una reserva confirmada.
     */
    @PostMapping
    @PreAuthorize("hasRole('MESERO')")
    public ResponseEntity<OrderResponseDTO> crearPedido(@RequestBody CreateOrderRequestDTO request) {
        logger.info("POST /api/orders - crear pedido");
        OrderResponseDTO response = orderService.crearPedido(request);
        meterRegistry.counter("redia.orders.created", "result", "success").increment();
        return ResponseEntity.ok(response);
    }

    /**
     * Ver pedidos activos del mesero autenticado.
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('MESERO')")
    public ResponseEntity<List<OrderResponseDTO>> misPedidos() {
        return ResponseEntity.ok(orderService.obtenerPedidosMesero());
    }

    /**
     * Enviar pedido a cocina (CREATED → IN_PROGRESS).
     */
    @PutMapping("/{id}/send-to-kitchen")
    @PreAuthorize("hasRole('MESERO')")
    public ResponseEntity<OrderResponseDTO> enviarACocina(@PathVariable String id) {
        logger.info("PUT /api/orders/{}/send-to-kitchen", id);
        return ResponseEntity.ok(orderService.enviarACocina(id));
    }

    /**
     * Obtener reservas CONFIRMADAS disponibles para crear un pedido.
     */
    @GetMapping("/reservations/active")
    @PreAuthorize("hasRole('MESERO')")
    public ResponseEntity<List<ReservationResponseDTO>> reservasActivas() {
        return ResponseEntity.ok(orderService.obtenerReservasActivas());
    }

    // ─────────────── COCINERO ───────────────

    /**
     * Ver cola de pedidos en cocina (CREATED + IN_PROGRESS).
     */
    @GetMapping("/kitchen")
    @PreAuthorize("hasRole('COCINERO')")
    public ResponseEntity<List<OrderResponseDTO>> pedidosCocina() {
        return ResponseEntity.ok(orderService.obtenerPedidosCocina());
    }

    /**
     * Marcar pedido como listo (IN_PROGRESS → READY).
     */
    @PutMapping("/{id}/mark-ready")
    @PreAuthorize("hasRole('COCINERO')")
    public ResponseEntity<OrderResponseDTO> marcarListo(@PathVariable String id) {
        logger.info("PUT /api/orders/{}/mark-ready", id);
        return ResponseEntity.ok(orderService.marcarListo(id));
    }

    // ─────────────── CAJERO ───────────────

    /**
     * Ver pedidos listos para cobrar (READY).
     */
    @GetMapping("/cashier")
    @PreAuthorize("hasRole('CAJERO')")
    public ResponseEntity<List<OrderResponseDTO>> pedidosCajero() {
        return ResponseEntity.ok(orderService.obtenerPedidosCajero());
    }

    /**
     * Registrar pago del pedido (READY → PAID).
     */
    @PutMapping("/{id}/pay")
    @PreAuthorize("hasRole('CAJERO')")
    public ResponseEntity<OrderResponseDTO> registrarPago(
            @PathVariable String id,
            @RequestBody PayOrderRequestDTO request) {
        logger.info("PUT /api/orders/{}/pay", id);
        Timer.Sample paymentTimer = Timer.start(meterRegistry);
        OrderResponseDTO response = orderService.registrarPago(id, request);
        String method = request.metodoPago().toUpperCase();

        meterRegistry.counter("redia.order.payments", "result", "success", "method", method).increment();
        paymentTimer.stop(Timer.builder("redia.order.payment.duration")
                .description("Tiempo que tarda el sistema en registrar el pago de un pedido listo")
                .tag("result", "success")
                .tag("method", method)
                .register(meterRegistry));

        return ResponseEntity.ok(response);
    }

    // ─────────────── COMPARTIDO ───────────────

    @GetMapping("/debug-all")
    public ResponseEntity<List<OrderResponseDTO>> debugAllOrders() {
        return ResponseEntity.ok(
            orderService.obtenerTodosParaDebug()
        );
    }

    /**
     * Detalle de un pedido (cualquier rol del personal).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESERO','COCINERO','CAJERO','ADMINISTRADOR')")
    public ResponseEntity<OrderResponseDTO> detallePedido(@PathVariable String id) {
        return ResponseEntity.ok(orderService.obtenerPedido(id));
    }
}
