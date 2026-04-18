package com.redia.back.service.impl;

import com.redia.back.dto.*;
import com.redia.back.exception.BadRequestException;
import com.redia.back.model.*;
import com.redia.back.repository.*;
import com.redia.back.service.OrderService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de gestión de pedidos.
 * Controla el flujo: CREATED → IN_PROGRESS → READY → PAID.
 */
@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final DishRepository dishRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            DishRepository dishRepository,
            ReservationRepository reservationRepository,
            UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.dishRepository = dishRepository;
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
    }

    // ─────────────────────────────────
    // Helper: usuario autenticado
    // ─────────────────────────────────
    private User obtenerUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Usuario autenticado no encontrado"));
    }

    // ─────────────────────────────────
    // Helper: mapear Order → DTO
    // ─────────────────────────────────
    private OrderResponseDTO toDTO(Order order) {
        List<OrderDishDTO> dishDTOs = order.getDishes() == null ? List.of() :
                order.getDishes().stream().map(od -> new OrderDishDTO(
                        od.getId(),
                        od.getDish().getNombre(),
                        od.getDish().getCategoria(),
                        od.getCantidad(),
                        od.getPrecioUnitario(),
                        od.getSubtotal(),
                        od.getNotas()
                )).collect(Collectors.toList());

        Reservation res = order.getReservation();
        User cliente = res.getCliente();

        return new OrderResponseDTO(
                order.getId(),
                order.getStatus().name(),
                res.getId(),
                cliente.getNombre(),
                cliente.getEmail(),
                order.getMesero().getNombre(),
                order.getFechaCreacion(),
                order.getTotal(),
                order.getNotas(),
                dishDTOs
        );
    }

    // ─────────────────────────────────
    // Crear pedido (MESERO)
    // ─────────────────────────────────
    @Override
    @Transactional
    public OrderResponseDTO crearPedido(CreateOrderRequestDTO request) {
        User mesero = obtenerUsuarioAutenticado();
        logger.info("Mesero {} creando pedido para reserva {}", mesero.getEmail(), request.reservationId());

        // Validar reserva
        Reservation reserva = reservationRepository.findById(request.reservationId())
                .orElseThrow(() -> new BadRequestException("Reserva no encontrada"));

        if (reserva.getEstado() != ReservationStatus.CONFIRMADA) {
            throw new BadRequestException("Solo se pueden crear pedidos para reservas CONFIRMADAS.");
        }

        // Verificar que no haya ya un pedido activo para esta reserva
        List<OrderStatus> terminados = List.of(OrderStatus.PAGADO, OrderStatus.CANCELADO);
        if (orderRepository.existsByReservationIdAndStatusNotIn(reserva.getId(), terminados)) {
            throw new BadRequestException("Ya existe un pedido activo para esta reserva.");
        }

        // Validar que haya ítems
        if (request.items() == null || request.items().isEmpty()) {
            throw new BadRequestException("El pedido debe contener al menos un plato.");
        }

        // Crear el pedido
        Order order = new Order(reserva, mesero, request.notas());
        order = orderRepository.save(order);

        // Agregar platos
        double total = 0.0;
        for (OrderItemRequestDTO item : request.items()) {
            Dish dish = dishRepository.findById(item.dishId())
                    .orElseThrow(() -> new BadRequestException("Plato no encontrado: " + item.dishId()));
            if (!dish.getAvailable()) {
                throw new BadRequestException("El plato '" + dish.getNombre() + "' no está disponible.");
            }
            OrderDishes orderDish = new OrderDishes(order, dish, item.cantidad(), dish.getPrecio(), item.notas());
            total += orderDish.getSubtotal();
            // Será guardado en cascada
            if (order.getDishes() == null) {
                order.setDishes(new java.util.ArrayList<>());
            }
            order.getDishes().add(orderDish);
        }

        order.setTotal(total);
        order = orderRepository.save(order);

        logger.info("Pedido {} creado con total ${}", order.getId(), total);
        return toDTO(order);
    }

    // ─────────────────────────────────
    // Pedidos del mesero autenticado
    // ─────────────────────────────────
    @Override
    public List<OrderResponseDTO> obtenerPedidosMesero() {
        User mesero = obtenerUsuarioAutenticado();
        return orderRepository.findByMeseroIdOrderByFechaCreacionDesc(mesero.getId())
                .stream()
                .filter(o -> o.getStatus() != OrderStatus.PAGADO && o.getStatus() != OrderStatus.CANCELADO)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────
    // Pedidos pendientes en cocina (COCINERO)
    // ─────────────────────────────────
    @Override
    public List<OrderResponseDTO> obtenerPedidosCocina() {
        return orderRepository.findByStatusIn(List.of(OrderStatus.PENDIENTE, OrderStatus.EN_PREPARACION))
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────
    // Pedidos listos para cobrar (CAJERO)
    // ─────────────────────────────────
    @Override
    public List<OrderResponseDTO> obtenerPedidosCajero() {
        return orderRepository.findByStatusIn(List.of(OrderStatus.LISTO))
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────
    // Detalle de un pedido
    // ─────────────────────────────────
    @Override
    public OrderResponseDTO obtenerPedido(String id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Pedido no encontrado"));
        return toDTO(order);
    }

    // ─────────────────────────────────
    // Enviar a cocina: CREATED → IN_PROGRESS (MESERO)
    // ─────────────────────────────────
    @Override
    @Transactional
    public OrderResponseDTO enviarACocina(String id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Pedido no encontrado"));
        if (order.getStatus() != OrderStatus.PENDIENTE) {
            throw new BadRequestException("El pedido ya fue enviado a cocina.");
        }
        order.setStatus(OrderStatus.EN_PREPARACION);
        orderRepository.save(order);
        logger.info("Pedido {} enviado a cocina", id);
        return toDTO(order);
    }

    // ─────────────────────────────────
    // Marcar listo: IN_PROGRESS → READY (COCINERO)
    // ─────────────────────────────────
    @Override
    @Transactional
    public OrderResponseDTO marcarListo(String id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Pedido no encontrado"));
        if (order.getStatus() != OrderStatus.EN_PREPARACION) {
            throw new BadRequestException("El pedido no está en preparación.");
        }
        order.setStatus(OrderStatus.LISTO);
        orderRepository.save(order);
        logger.info("Pedido {} marcado como READY", id);
        return toDTO(order);
    }

    // ─────────────────────────────────
    // Registrar pago: READY → PAID (CAJERO)
    // ─────────────────────────────────
    @Override
    @Transactional
    public OrderResponseDTO registrarPago(String id, PayOrderRequestDTO request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Pedido no encontrado"));
        if (order.getStatus() != OrderStatus.LISTO) {
            throw new BadRequestException("El pedido no está listo para ser cobrado.");
        }

        PaymentMethod metodo;
        try {
            metodo = PaymentMethod.valueOf(request.metodoPago().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Método de pago inválido: " + request.metodoPago());
        }

        OrderPayment pago = new OrderPayment(order, metodo, order.getTotal());
        pago.setStatus(PaymentStatus.PAGADO);
        order.setPayment(pago);
        order.setStatus(OrderStatus.PAGADO);
        orderRepository.save(order);

        logger.info("Pedido {} marcado como PAID con método {}", id, metodo);
        return toDTO(order);
    }

    // ─────────────────────────────────
    // Platos disponibles (MESERO)
    // ─────────────────────────────────
    @Override
    public List<DishResponseDTO> obtenerPlatosDisponibles() {
        return dishRepository.findByAvailableTrue().stream()
                .map(d -> new DishResponseDTO(
                        d.getId(),
                        d.getNombre(),
                        d.getDescripcion(),
                        d.getPrecio(),
                        d.getCategoria(),
                        d.getImageUrl(),
                        d.getAvailable()))
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────
    // Reservas activas CONFIRMADAS (MESERO — para crear pedido)
    // ─────────────────────────────────
    @Override
    public List<ReservationResponseDTO> obtenerReservasActivas() {
        return reservationRepository.findAll().stream()
                .filter(r -> r.getEstado() == ReservationStatus.CONFIRMADA
                        && r.getHoraFinReserva().isAfter(LocalDateTime.now()))
                .map(r -> new ReservationResponseDTO(
                        r.getId(),
                        r.getCliente().getEmail(),
                        r.getCliente().getNombre(),
                        r.getFechaReserva(),
                        r.getHoraFinReserva(),
                        r.getNumeroPersonas(),
                        r.getEstado().name()))
                .collect(Collectors.toList());
    }
}
