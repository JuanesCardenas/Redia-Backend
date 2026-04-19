package com.redia.back.controller;

import com.redia.back.dto.DishRequestDTO;
import com.redia.back.dto.DishResponseDTO;
import com.redia.back.service.OrderService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para consultar y gestionar el menú.
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
    @PreAuthorize("hasAnyRole('MESERO','ADMINISTRADOR','COCINERO')")
    public ResponseEntity<List<DishResponseDTO>> platosDisponibles() {
        return ResponseEntity.ok(orderService.obtenerPlatosDisponibles());
    }

    /**
     * Obtener TODOS los platos (incluyendo no disponibles).
     * Solo para ADMINISTRADOR.
     */
    @GetMapping("/dishes/all")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<DishResponseDTO>> todosLosPlatos() {
        return ResponseEntity.ok(orderService.obtenerTodosPlatos());
    }

    /**
     * Crear un nuevo plato.
     * Solo para ADMINISTRADOR.
     */
    @PostMapping(value = "/dishes", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<DishResponseDTO> crearPlato(
            @RequestParam String nombre,
            @RequestParam String descripcion,
            @RequestParam Double precio,
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) org.springframework.web.multipart.MultipartFile image) {
        
        DishRequestDTO request = new DishRequestDTO(nombre, descripcion, precio, available);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.crearPlato(request, image));
    }

    /**
     * Editar un plato existente.
     * Solo para ADMINISTRADOR.
     */
    @PutMapping(value = "/dishes/{id}", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<DishResponseDTO> editarPlato(
            @PathVariable String id,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) Double precio,
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) org.springframework.web.multipart.MultipartFile image) {
        
        DishRequestDTO request = new DishRequestDTO(nombre, descripcion, precio, available);
        return ResponseEntity.ok(orderService.actualizarPlato(id, request, image));
    }

    /**
     * Eliminar un plato.
     * Solo para ADMINISTRADOR.
     */
    @DeleteMapping("/dishes/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminarPlato(@PathVariable String id) {
        orderService.eliminarPlato(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Alternar la disponibilidad de un plato (activar/desactivar).
     * Solo para ADMINISTRADOR.
     */
    @PatchMapping("/dishes/{id}/toggle")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<DishResponseDTO> toggleDisponibilidad(@PathVariable String id) {
        return ResponseEntity.ok(orderService.toggleDisponibilidad(id));
    }
}
