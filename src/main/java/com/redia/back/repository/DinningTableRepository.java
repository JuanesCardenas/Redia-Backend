package com.redia.back.repository;

import com.redia.back.model.DinningTable;
import com.redia.back.model.DinningTableStatus;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DinningTableRepository extends JpaRepository<DinningTable, String> {

    List<DinningTable> findAllByEstadoMesa(DinningTableStatus estadoMesa);

    void deleteById(String id);
}
