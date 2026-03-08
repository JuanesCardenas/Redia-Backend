package com.redia.back.controller;

import com.redia.back.dto.CreateDinningTableRequestDTO;
import com.redia.back.model.DinningTable;
import com.redia.back.model.DinningTableStatus;
import com.redia.back.service.DinningTableService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dinning-tables")
@PreAuthorize("hasRole('ADMIN')")
public class DinningTableController {

    private final DinningTableService dinningTableService;

    public DinningTableController(DinningTableService dinningTableService) {
        this.dinningTableService = dinningTableService;
    }

    @PostMapping
    public ResponseEntity<DinningTable> createDinningTable(
            @Valid @RequestBody CreateDinningTableRequestDTO request) {
        DinningTable dinningTable = dinningTableService.createDinningTable(request);
        return ResponseEntity.ok(dinningTable);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDinningTable(@PathVariable String id) {
        dinningTableService.deleteDinningTable(id);
        return ResponseEntity.ok("Mesa eliminada correctamente");
    }

    @GetMapping
    public ResponseEntity<List<DinningTable>> getAllDinningTables() {
        List<DinningTable> dinningTables = dinningTableService.findAllByEstadoMesa(DinningTableStatus.DISPONIBLE);
        return ResponseEntity.ok(dinningTables);
    }

    @GetMapping("/available")
    public ResponseEntity<List<DinningTable>> getAllAvailableDinningTables() {
        List<DinningTable> dinningTables = dinningTableService.findAllByEstadoMesa(DinningTableStatus.DISPONIBLE);
        return ResponseEntity.ok(dinningTables);
    }

    @GetMapping("/reserved")
    public ResponseEntity<List<DinningTable>> getAllReservedDinningTables() {
        List<DinningTable> dinningTables = dinningTableService.findAllByEstadoMesa(DinningTableStatus.RESERVADA);
        return ResponseEntity.ok(dinningTables);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DinningTable> getDinningTableById(@PathVariable String id) {
        DinningTable dinningTable = dinningTableService.getDinningTableById(id);
        return ResponseEntity.ok(dinningTable);
    }

}
