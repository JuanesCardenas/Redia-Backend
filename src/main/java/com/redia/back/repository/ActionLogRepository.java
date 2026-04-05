package com.redia.back.repository;

import com.redia.back.model.ActionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActionLogRepository extends JpaRepository<ActionLog, Long> {

    List<ActionLog> findByEmailUsuarioOrderByFechaDesc(String emailUsuario);

    List<ActionLog> findByAccionOrderByFechaDesc(String accion);
}