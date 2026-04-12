package com.redia.back.service.impl;

import com.redia.back.dto.CreateDinningTableRequestDTO;
import com.redia.back.dto.UpdateDinningTableRequestDTO;
import com.redia.back.exception.BadRequestException;
import com.redia.back.model.DinningTable;
import com.redia.back.model.User;
import com.redia.back.repository.DinningTableRepository;
import com.redia.back.repository.UserRepository;
import com.redia.back.service.DinningTableService;
import com.redia.back.service.ActionLogService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final ActionLogService actionLogService;
    private final UserRepository userRepository;

    public DinningTableServiceImpl(DinningTableRepository diningTableRepository, ActionLogService actionLogService,
            UserRepository userRepository) {
        this.diningTableRepository = diningTableRepository;
        this.actionLogService = actionLogService;
        this.userRepository = userRepository;
    }

    /**
     * Obtener usuario autenticado
     */
    private User obtenerUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado"));
    }

    @Override
    public DinningTable createDinningTable(CreateDinningTableRequestDTO request) {

        User usuario = obtenerUsuarioAutenticado();

        String generatedId = java.util.UUID.randomUUID().toString();

        DinningTable diningTable = new DinningTable(
                generatedId,
                request.nombre(),
                request.capacidad());

        actionLogService.registrar(usuario, "CREAR_MESA",
                "Mesa creada con ID: " + generatedId + " - Nombre: " + request.nombre());

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

        User usuario = obtenerUsuarioAutenticado();

        DinningTable diningTable = diningTableRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("La mesa no existe."));

        diningTable.setNombre(request.nombre());
        diningTable.setCapacidad(request.capacidad());

        actionLogService.registrar(usuario, "ACTUALIZAR_MESA", "Mesa actualizada ID: " + id);

        return diningTableRepository.save(diningTable);
    }

    /**
     * Elimina una mesa del sistema por su identificador.
     */
    @Override
    public void deleteDinningTable(String id) {
        User usuario = obtenerUsuarioAutenticado();

        DinningTable diningTable = diningTableRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("La mesa no existe."));

        diningTableRepository.delete(diningTable);
        actionLogService.registrar(usuario, "ELIMINAR_MESA",
                "Mesa eliminada ID: " + id + " - Nombre: " + diningTable.getNombre());
    }
}