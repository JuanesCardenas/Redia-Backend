package com.redia.back.service.impl;

import com.redia.back.dto.CreateDinningTableRequestDTO;
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
 * - Buscar mesas por estado
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

        DinningTable diningTable = new DinningTable(
                request.nombre(),
                Integer.parseInt(request.capacidad()));

        return diningTableRepository.save(diningTable);
    }

    /**
     * Obtiene la mesas por id en el sistema.
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
     * Elimina una mesa del sistema por su identificador.
     */
    @Override
    public void deleteDinningTable(String id) {

        DinningTable diningTable = diningTableRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("La mesa no existe."));

        diningTableRepository.delete(diningTable);
    }
}