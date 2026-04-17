package com.redia.back.service.impl;

import com.redia.back.dto.CreateOrderRequestDTO;
import com.redia.back.dto.OrderDishResponseDTO;
import com.redia.back.dto.OrderResponseDTO;
import com.redia.back.exception.BadRequestException;
import com.redia.back.model.*;
import com.redia.back.repository.*;
import com.redia.back.service.OrderService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final ReservationRepository reservationRepository;
    private final DishRepository dishRepository;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            ReservationRepository reservationRepository,
            DishRepository dishRepository) {

        this.orderRepository = orderRepository;
        this.reservationRepository = reservationRepository;
        this.dishRepository = dishRepository;
    }

    /**
     * Crear pedido desde una reserva
     */
    @Override
    public OrderResponseDTO crearPedido(CreateOrderRequestDTO request) {

        logger.info("Creando pedido para reserva {}", request.reservationId());

        // Validar reserva
        Reservation reservation = reservationRepository.findById(request.reservationId())
                .orElseThrow(() -> new BadRequestException("Reserva no encontrada"));

        if (reservation.getEstado() != ReservationStatus.CONFIRMADA) {
            throw new BadRequestException("Solo se pueden crear pedidos para reservas confirmadas");
        }

        // Validar que no exista ya un pedido para esa reserva
        boolean exists = orderRepository.existsByReservationId(reservation.getId());
        if (exists) {
            throw new BadRequestException("Ya existe un pedido para esta reserva");
        }

        Order order = new Order();
        order.setReservation(reservation);
        order.setFechaCreacion(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDIENTE);

        List<OrderDishes> dishes = request.dishes().stream().map(d -> {

            Dish dish = dishRepository.findById(d.dishId())
                    .orElseThrow(() -> new BadRequestException("Plato no encontrado"));

            OrderDishes od = new OrderDishes();
            od.setOrder(order);
            od.setDish(dish);
            od.setCantidad(d.cantidad());
            od.setPrecioUnitario(dish.getPrecio());

            return od;

        }).toList();

        order.setDishes(dishes);

        // 4. Total
        double total = dishes.stream()
                .mapToDouble(d -> d.getCantidad() * d.getPrecioUnitario())
                .sum();

        order.setTotal(total);

        // 5. Guardar
        orderRepository.save(order);

        logger.info("Pedido creado con ID {}", order.getId());

        return mapToDTO(order);
    }

    /**
     * Obtener todos los pedidos
     */
    @Override
    public List<OrderResponseDTO> obtenerTodos() {
        return orderRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener pedido por ID
     */
    @Override
    public OrderResponseDTO obtenerPorId(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BadRequestException("Pedido no encontrado"));

        return mapToDTO(order);
    }

    /**
     * Cambiar estado del pedido
     */
    @Override
    public void cambiarEstado(String orderId, String nuevoEstado) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BadRequestException("Pedido no encontrado"));

        try {
            OrderStatus status = OrderStatus.valueOf(nuevoEstado);
            order.setStatus(status);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Estado inválido");
        }

        orderRepository.save(order);

        logger.info("Pedido {} actualizado a estado {}", orderId, nuevoEstado);
    }

    /**
     * Mapper a DTO
     */
    private OrderResponseDTO mapToDTO(Order order) {

        List<OrderDishResponseDTO> dishes = order.getDishes().stream().map(d -> new OrderDishResponseDTO(
                d.getDish().getId(),
                d.getDish().getNombre(),
                d.getCantidad(),
                d.getPrecioUnitario(),
                d.getCantidad() * d.getPrecioUnitario())).collect(Collectors.toList());

        return new OrderResponseDTO(
                order.getId(),
                order.getReservation().getId(),
                order.getFechaCreacion(),
                order.getStatus().name(),
                order.getTotal(),
                dishes);
    }
}