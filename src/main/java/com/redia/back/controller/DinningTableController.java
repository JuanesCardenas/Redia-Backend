package com.redia.back.controller;

import com.redia.back.dto.CreateDinningTableRequestDTO;
import com.redia.back.dto.UpdateDinningTableRequestDTO;
import com.redia.back.model.DinningTable;
import com.redia.back.service.DinningTableService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST encargado de gestionar las mesas del restaurante.
 *
 * Solo los usuarios con rol ADMIN pueden acceder a estos endpoints.
 */
@RestController
@RequestMapping("/api/admin/dinning-tables")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class DinningTableController {

    private final DinningTableService dinningTableService;

    public DinningTableController(DinningTableService dinningTableService) {
        this.dinningTableService = dinningTableService;
    }

    /**
     * Crear una nueva mesa en el sistema.
     */
    @PostMapping
    public ResponseEntity<DinningTable> createDinningTable(
            @Valid @RequestBody CreateDinningTableRequestDTO request) {

        DinningTable dinningTable = dinningTableService.createDinningTable(request);
        return ResponseEntity.ok(dinningTable);
    }

    /**
     * Obtener una mesa por su identificador.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DinningTable> getDinningTableById(@PathVariable String id) {

        DinningTable dinningTable = dinningTableService.getDinningTableById(id);
        return ResponseEntity.ok(dinningTable);
    }

    /**
     * Obtener todas las mesas registradas en el sistema.
     */
    @GetMapping
    public ResponseEntity<List<DinningTable>> getAllDinningTables() {

        List<DinningTable> tables = dinningTableService.getAllDinningTables();
        return ResponseEntity.ok(tables);
    }

    /**
     * Actualizar los datos de una mesa existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<DinningTable> updateDinningTable(
            @PathVariable String id,
            @Valid @RequestBody UpdateDinningTableRequestDTO request) {

        DinningTable updated = dinningTableService.updateDinningTable(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Eliminar una mesa del sistema.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDinningTable(@PathVariable String id) {

        dinningTableService.deleteDinningTable(id);
        return ResponseEntity.ok("Mesa eliminada correctamente");
    }
}