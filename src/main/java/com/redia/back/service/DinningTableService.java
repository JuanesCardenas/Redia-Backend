package com.redia.back.service;

import java.util.List;

import com.redia.back.model.DinningTable;
import com.redia.back.dto.CreateDinningTableRequestDTO;

public interface DinningTableService {

    DinningTable createDinningTable(CreateDinningTableRequestDTO request);

    DinningTable getDinningTableById(String id);

    List<DinningTable> getAllDinningTables();

    void deleteDinningTable(String id);

}
