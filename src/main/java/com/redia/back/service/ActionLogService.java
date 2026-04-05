package com.redia.back.service;

import com.redia.back.model.User;

public interface ActionLogService {
    void registrar(User usuario, String accion, String detalle);

    void registrarSinUsuario(String email, String accion, String detalle);
}
