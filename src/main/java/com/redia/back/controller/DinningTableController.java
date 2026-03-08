package com.redia.back.controller;

import com.redia.back.dto.CreateDinningTableRequestDTO;
import com.redia.back.model.DinningTable;
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

    @GetMapping("/{id}")
    public ResponseEntity<DinningTable> getDinningTableById(@PathVariable String id) {
        DinningTable dinningTable = dinningTableService.getDinningTableById(id);
        return ResponseEntity.ok(dinningTable);
    }

}
