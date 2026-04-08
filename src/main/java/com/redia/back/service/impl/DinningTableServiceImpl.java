package com.redia.back.service.impl;

import com.redia.back.dto.CreateDinningTableRequestDTO;
import com.redia.back.dto.UpdateDinningTableRequestDTO;
import com.redia.back.exception.BadRequestException;
import com.redia.back.model.DinningTable;
import com.redia.back.repository.DinningTableRepository;
import com.redia.back.service.DinningTableService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación del servicio encargado de gestionar las mesas del restaurante.
 *
 * Este servicio permite:
 * - Crear mesas
 * - Obtener todas las mesas
 * - Obtener mesa por ID
 * - Actualizar mesas
 * - Eliminar mesas
 */
@Service
@Transactional
public class DinningTableServiceImpl implements DinningTableService {

    private final DinningTableRepository diningTableRepository;

    /**
     * Constructor con inyección de dependencias.
     */
    public DinningTableServiceImpl(DinningTableRepository diningTableRepository) {
        this.diningTableRepository = diningTableRepository;
    }

    /**
     * Crea una nueva mesa en el sistema.
     *
     * La mesa se crea con estado DISPONIBLE por defecto.
     */
    @Override
    public DinningTable createDinningTable(CreateDinningTableRequestDTO request) {

        String generatedId = java.util.UUID.randomUUID().toString();

        DinningTable diningTable = new DinningTable(
                generatedId,
                request.nombre(),
                request.capacidad());

        return diningTableRepository.save(diningTable);
    }

    /**
     * Obtiene una mesa por su identificador.
     */
    @Override
    public DinningTable getDinningTableById(String id) {

        return diningTableRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("La mesa no existe."));
    }

    /**
     * Obtiene todas las mesas registradas en el sistema.
     */
    @Override
    public List<DinningTable> getAllDinningTables() {

        return diningTableRepository.findAll();
    }

    /**
     * Actualiza los datos de una mesa existente.
     */
    @Override
    public DinningTable updateDinningTable(String id, UpdateDinningTableRequestDTO request) {

        DinningTable diningTable = diningTableRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("La mesa no existe."));

        diningTable.setNombre(request.nombre());
        diningTable.setCapacidad(request.capacidad());

        return diningTableRepository.save(diningTable);
    }

    /**
     * Elimina una mesa del sistema por su identificador.
     */
    @Override
    public void deleteDinningTable(String id) {

        DinningTable diningTable = diningTableRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("La mesa no existe."));

        diningTableRepository.delete(diningTable);
    }
}