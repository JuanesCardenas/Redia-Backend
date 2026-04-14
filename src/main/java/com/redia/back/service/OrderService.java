package com.redia.back.service;

import com.redia.back.dto.CreateOrderRequestDTO;
import com.redia.back.dto.OrderResponseDTO;

import java.util.List;

public interface OrderService {

    /**
     * Crear un pedido a partir de una reserva existente.
     */
    OrderResponseDTO crearPedido(CreateOrderRequestDTO request);

    /**
     * Obtener todos los pedidos.
     */
    List<OrderResponseDTO> obtenerTodos();

    /**
     * Obtener un pedido por ID.
     */
    OrderResponseDTO obtenerPorId(String orderId);

    /**
     * Cambiar estado del pedido.
     */
    void cambiarEstado(String orderId, String nuevoEstado);
}