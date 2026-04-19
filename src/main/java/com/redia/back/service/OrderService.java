package com.redia.back.service;

import com.redia.back.dto.CreateOrderRequestDTO;
import com.redia.back.dto.DishRequestDTO;
import com.redia.back.dto.DishResponseDTO;
import com.redia.back.dto.OrderResponseDTO;
import com.redia.back.dto.PayOrderRequestDTO;

import java.util.List;

public interface OrderService {

    /** Crear un nuevo pedido (MESERO) */
    OrderResponseDTO crearPedido(CreateOrderRequestDTO request);

    /** Ver pedidos activos asignados al mesero autenticado */
    List<OrderResponseDTO> obtenerPedidosMesero();

    /** Ver todos los pedidos en estado CREATED o IN_PROGRESS (COCINERO) */
    List<OrderResponseDTO> obtenerPedidosCocina();

    /** Ver todos los pedidos en estado READY (CAJERO) */
    List<OrderResponseDTO> obtenerPedidosCajero();

    /** Obtener detalle de un pedido por ID */
    OrderResponseDTO obtenerPedido(String id);

    /** Enviar pedido a cocina: CREATED → IN_PROGRESS (MESERO) */
    OrderResponseDTO enviarACocina(String id);

    /** Marcar pedido como listo: IN_PROGRESS → READY (COCINERO) */
    OrderResponseDTO marcarListo(String id);

    /** Registrar pago: READY → PAID (CAJERO) */
    OrderResponseDTO registrarPago(String id, PayOrderRequestDTO request);

    /** Obtener platos disponibles del menú (MESERO) */
    List<DishResponseDTO> obtenerPlatosDisponibles();

    /** Obtener reservas CONFIRMADAS activas (para el mesero al crear pedido) */
    List<com.redia.back.dto.ReservationResponseDTO> obtenerReservasActivas();

    // ─── CRUD de platos (ADMINISTRADOR) ───────────────────────────────────

    /** Obtener TODOS los platos (incluyendo no disponibles) — solo ADMIN */
    List<DishResponseDTO> obtenerTodosPlatos();

    /** Crear un plato nuevo */
    DishResponseDTO crearPlato(DishRequestDTO request, org.springframework.web.multipart.MultipartFile image);

    /** Editar un plato existente */
    DishResponseDTO actualizarPlato(String id, DishRequestDTO request, org.springframework.web.multipart.MultipartFile image);

    /** Eliminar un plato */
    void eliminarPlato(String id);

    /** Alternar disponibilidad de un plato */
    DishResponseDTO toggleDisponibilidad(String id);
}
