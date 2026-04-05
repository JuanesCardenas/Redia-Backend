package com.redia.back.service.impl;

import com.redia.back.model.ActionLog;
import com.redia.back.model.User;
import com.redia.back.repository.ActionLogRepository;
import com.redia.back.service.ActionLogService;

import org.springframework.stereotype.Service;

@Service
public class ActionLogServiceImpl implements ActionLogService {
    private final ActionLogRepository actionLogRepository;

    public ActionLogServiceImpl(ActionLogRepository actionLogRepository) {
        this.actionLogRepository = actionLogRepository;
    }

    @Override
    public void registrar(User usuario, String accion, String detalle) {
        ActionLog log = new ActionLog();
        log.setNombreUsuario(usuario.getNombre());
        log.setEmailUsuario(usuario.getEmail());
        log.setRolUsuario(usuario.getRole().name());
        log.setAccion(accion);
        log.setDetalle(detalle);
        actionLogRepository.save(log);
        System.out.println(">>> ACTION LOG GUARDADO con id: " + log.getId()); // línea temporal

    }

    // Para acciones sin usuario aún (login fallido, forgot password, etc.)
    @Override
    public void registrarSinUsuario(String email, String accion, String detalle) {
        ActionLog log = new ActionLog();
        log.setNombreUsuario("Desconocido");
        log.setEmailUsuario(email);
        log.setRolUsuario("NINGUNO");
        log.setAccion(accion);
        log.setDetalle(detalle);
        actionLogRepository.save(log);
    }
}
