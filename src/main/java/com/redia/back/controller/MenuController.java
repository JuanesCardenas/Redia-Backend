package com.redia.back.controller;

import com.redia.back.dto.DishResponseDTO;
import com.redia.back.service.OrderService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para consultar el menú disponible.
 */
@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final OrderService orderService;

    public MenuController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Obtener platos disponibles (available = true).
     * Accesible por el mesero para crear pedidos.
     */
    @GetMapping("/dishes")
    @PreAuthorize("hasAnyAuthority('MESERO','ADMINISTRADOR','COCINERO')")
    public ResponseEntity<List<DishResponseDTO>> platosDisponibles() {
        return ResponseEntity.ok(orderService.obtenerPlatosDisponibles());
    }
}
