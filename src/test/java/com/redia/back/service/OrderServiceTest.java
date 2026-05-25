package com.redia.back.service;

import com.redia.back.dto.CreateOrderRequestDTO;
import com.redia.back.dto.OrderItemRequestDTO;
import com.redia.back.dto.OrderResponseDTO;
import com.redia.back.exception.BadRequestException;
import com.redia.back.model.*;
import com.redia.back.repository.*;
import com.redia.back.service.impl.OrderServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para OrderServiceImpl.
 *
 * C63 (F) → Creación de pedido vinculado a reserva existente
 * C64 (F) → Selección de múltiples platillos y cantidades
 * C67 (F) → Flujo de estado: Envío a cocina
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private DishRepository dishRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ImageService imageService;

    @Mock
    private ActionLogService actionLogService;

    // Mocks de Spring Security para simular usuario autenticado (el mesero)
    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private OrderServiceImpl orderService;

    // -----------------------------------------------------------------------
    // Datos reutilizables
    // -----------------------------------------------------------------------

    private static final String EMAIL_MESERO = "mesero@redia.com";
    private static final String NOMBRE_MESERO = "Carlos Mesero";
    private static final String RESERVA_ID = "reserva-001";
    private static final String PEDIDO_ID = "pedido-001";
    private static final String DISH_ID_1 = "plato-001";
    private static final String DISH_ID_2 = "plato-002";

    private User meseroMock;
    private Reservation reservaMock;
    private Dish platoMock1;
    private Dish platoMock2;

    @BeforeEach
    void setUp() {
        // Mesero autenticado simulado
        meseroMock = new User(NOMBRE_MESERO, EMAIL_MESERO, "3001112233", "hash", Role.MESERO);
        meseroMock.setId("mesero-id-001");

        // Reserva CONFIRMADA simulada con ArrayList mutable
        User clienteMock = new User("Cliente Test", "cliente@test.com", "3009998877", "hash", Role.CLIENTE);
        reservaMock = new Reservation(
                clienteMock,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3),
                4,
                new ArrayList<>(),
                ReservationStatus.CONFIRMADA);
        reservaMock.setId(RESERVA_ID);

        // Platos disponibles simulados
        platoMock1 = new Dish("Bandeja Paisa", "Plato típico", 25000.0, "url1", true);
        platoMock1.setId(DISH_ID_1);

        platoMock2 = new Dish("Ajiaco", "Sopa bogotana", 18000.0, "url2", true);
        platoMock2.setId(DISH_ID_2);
    }

    private void configurarMeseroAutenticado() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(EMAIL_MESERO);
        when(userRepository.findByEmail(EMAIL_MESERO)).thenReturn(Optional.of(meseroMock));
    }

    // -----------------------------------------------------------------------
    // C63 — Creación de pedido vinculado a reserva existente
    // -----------------------------------------------------------------------

    /**
     * C63 (F): Dado un mesero autenticado y una reserva CONFIRMADA existente,
     * el servicio debe crear el pedido correctamente, vincularlo a esa reserva
     * y retornar un OrderResponseDTO con estado PENDIENTE.
     */
    @Test
    @DisplayName("C63 - Creacion de pedido vinculado a reserva existente")
    void c63_creacionPedidoVinculadoAReserva() {

        // --- Arrange ---
        configurarMeseroAutenticado();

        CreateOrderRequestDTO request = new CreateOrderRequestDTO(
                RESERVA_ID,
                "Sin cebolla por favor",
                List.of(new OrderItemRequestDTO(DISH_ID_1, 1, "")));

        // La reserva existe y está CONFIRMADA
        when(reservationRepository.findById(RESERVA_ID))
                .thenReturn(Optional.of(reservaMock));

        // El plato existe y está disponible
        when(dishRepository.findById(DISH_ID_1))
                .thenReturn(Optional.of(platoMock1));

        // El repositorio guarda y devuelve el pedido con ID asignado
        Order orderGuardado = new Order(reservaMock, meseroMock, "Sin cebolla por favor");
        orderGuardado.setId(PEDIDO_ID);
        orderGuardado.setDishes(new ArrayList<>(List.of(
                new OrderDishes(orderGuardado, platoMock1, 1, platoMock1.getPrecio(), ""))));
        orderGuardado.setTotal(platoMock1.getPrecio());
        when(orderRepository.save(any(Order.class))).thenReturn(orderGuardado);

        // --- Act ---
        OrderResponseDTO resultado = orderService.crearPedido(request);

        // --- Assert ---
        assertNotNull(resultado, "El DTO de respuesta no debe ser null");
        assertEquals(PEDIDO_ID, resultado.id(), "El ID del pedido debe coincidir");
        assertEquals(RESERVA_ID, resultado.reservationId(), "El pedido debe estar vinculado a la reserva correcta");
        assertEquals(OrderStatus.PENDIENTE.name(), resultado.status(), "El pedido debe iniciar en estado PENDIENTE");
        assertEquals(NOMBRE_MESERO, resultado.meseroNombre(), "El mesero asignado debe coincidir");

        // El pedido se guardó en el repositorio
        verify(orderRepository, atLeast(1)).save(any(Order.class));

        // Se registró la acción en el log
        verify(actionLogService, times(1))
                .registrar(any(User.class), eq("CREAR_PEDIDO"), anyString());
    }

    // -----------------------------------------------------------------------
    // C64 — Selección de múltiples platillos y cantidades
    // -----------------------------------------------------------------------

    /**
     * C64 (F): Al crear un pedido con varios ítems (distintos platos y
     * cantidades), el total debe calcularse correctamente como la suma
     * de (precio × cantidad) de cada ítem.
     *
     * Plato 1: $25.000 × 2 = $50.000
     * Plato 2: $18.000 × 3 = $54.000
     * Total esperado: $104.000
     */
    @Test
    @DisplayName("C64 - Seleccion de multiples platillos y cantidades")
    void c64_seleccionMultiplesPlatillosYCantidades() {

        // --- Arrange ---
        configurarMeseroAutenticado();

        List<OrderItemRequestDTO> items = List.of(
                new OrderItemRequestDTO(DISH_ID_1, 2, ""), // 2 × $25.000
                new OrderItemRequestDTO(DISH_ID_2, 3, "Extra picante") // 3 × $18.000
        );

        CreateOrderRequestDTO request = new CreateOrderRequestDTO(
                RESERVA_ID,
                "Mesa para 5",
                items);

        when(reservationRepository.findById(RESERVA_ID))
                .thenReturn(Optional.of(reservaMock));

        when(dishRepository.findById(DISH_ID_1)).thenReturn(Optional.of(platoMock1));
        when(dishRepository.findById(DISH_ID_2)).thenReturn(Optional.of(platoMock2));

        // Construimos el pedido con los dos platos y el total correcto
        Order orderGuardado = new Order(reservaMock, meseroMock, "Mesa para 5");
        orderGuardado.setId(PEDIDO_ID);

        OrderDishes item1 = new OrderDishes(orderGuardado, platoMock1, 2, 25000.0, "");
        OrderDishes item2 = new OrderDishes(orderGuardado, platoMock2, 3, 18000.0, "Extra picante");
        orderGuardado.setDishes(new ArrayList<>(List.of(item1, item2)));

        double totalEsperado = (25000.0 * 2) + (18000.0 * 3); // 104.000
        orderGuardado.setTotal(totalEsperado);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order saved = invocation.getArgument(0);
            // En la primera llamada (crear pedido vacío) devuelve el order sin platos
            // En la segunda llamada (después de agregar platos) devuelve el orderGuardado
            if (saved.getDishes() != null && saved.getDishes().size() >= 2) {
                return orderGuardado;
            }
            Order inicial = new Order(reservaMock, meseroMock, "Mesa para 5");
            inicial.setId(PEDIDO_ID);
            inicial.setDishes(new ArrayList<>());
            inicial.setTotal(0.0);
            return inicial;
        });

        // --- Act ---
        OrderResponseDTO resultado = orderService.crearPedido(request);

        // --- Assert ---

        // El total debe ser la suma correcta de todos los ítems
        assertEquals(
                totalEsperado,
                resultado.total(),
                0.01, // tolerancia de centavos por aritmética de punto flotante
                "El total debe ser la suma de precio x cantidad de cada plato");

        // El pedido debe contener exactamente 2 ítems distintos
        assertEquals(
                2,
                resultado.dishes().size(),
                "El pedido debe contener exactamente 2 platos distintos");

        // Verificar subtotales individuales de cada ítem
        double subtotalPlato1 = resultado.dishes().stream()
                .filter(d -> d.dishNombre().equals("Bandeja Paisa"))
                .mapToDouble(d -> d.subtotal())
                .sum();
        assertEquals(50000.0, subtotalPlato1, 0.01, "Subtotal Bandeja Paisa: 2 x $25.000 = $50.000");

        double subtotalPlato2 = resultado.dishes().stream()
                .filter(d -> d.dishNombre().equals("Ajiaco"))
                .mapToDouble(d -> d.subtotal())
                .sum();
        assertEquals(54000.0, subtotalPlato2, 0.01, "Subtotal Ajiaco: 3 x $18.000 = $54.000");
    }

    // -----------------------------------------------------------------------
    // C67 — Flujo de estado: Envío a cocina
    // -----------------------------------------------------------------------

    /**
     * C67 (F): Un pedido en estado PENDIENTE, al ser enviado a cocina
     * mediante enviarACocina(), debe cambiar su estado a EN_PREPARACION.
     * Si se intenta enviar un pedido que ya no está en PENDIENTE,
     * debe lanzar BadRequestException.
     */
    @Test
    @DisplayName("C67 - Flujo de estado: Envio a cocina")
    void c67_flujoEstadoEnvioACocina() {

        // --- Arrange ---

        // Pedido en estado PENDIENTE (estado inicial tras ser creado)
        Order orderPendiente = new Order(reservaMock, meseroMock, "");
        orderPendiente.setId(PEDIDO_ID);
        orderPendiente.setStatus(OrderStatus.PENDIENTE);
        orderPendiente.setDishes(new ArrayList<>());
        orderPendiente.setTotal(0.0);

        when(orderRepository.findById(PEDIDO_ID)).thenReturn(Optional.of(orderPendiente));

        // Al guardar, devuelve el pedido ya con estado EN_PREPARACION
        Order orderEnPreparacion = new Order(reservaMock, meseroMock, "");
        orderEnPreparacion.setId(PEDIDO_ID);
        orderEnPreparacion.setStatus(OrderStatus.EN_PREPARACION);
        orderEnPreparacion.setDishes(new ArrayList<>());
        orderEnPreparacion.setTotal(0.0);
        when(orderRepository.save(any(Order.class))).thenReturn(orderEnPreparacion);

        // --- Act ---
        OrderResponseDTO resultado = orderService.enviarACocina(PEDIDO_ID);

        // --- Assert ---

        // El estado debe haber cambiado a EN_PREPARACION
        assertEquals(
                OrderStatus.EN_PREPARACION.name(),
                resultado.status(),
                "El pedido debe pasar a estado EN_PREPARACION tras ser enviado a cocina");

        // Se guardó el cambio de estado
        verify(orderRepository, times(1)).save(any(Order.class));

        // --- Caso negativo: intentar enviar a cocina un pedido ya en preparación ---
        Order orderYaEnviado = new Order(reservaMock, meseroMock, "");
        orderYaEnviado.setId("pedido-002");
        orderYaEnviado.setStatus(OrderStatus.EN_PREPARACION); // ya no está PENDIENTE
        orderYaEnviado.setDishes(new ArrayList<>());

        when(orderRepository.findById("pedido-002")).thenReturn(Optional.of(orderYaEnviado));

        BadRequestException excepcion = assertThrows(
                BadRequestException.class,
                () -> orderService.enviarACocina("pedido-002"),
                "Debe lanzar excepcion si el pedido ya fue enviado a cocina");

        assertEquals("El pedido ya fue enviado a cocina.", excepcion.getMessage());
    }
}
