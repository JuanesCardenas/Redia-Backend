package com.redia.back.service;

import java.util.List;

import com.redia.back.model.DinningTable;
import com.redia.back.dto.CreateDinningTableRequestDTO;
import com.redia.back.dto.UpdateDinningTableRequestDTO;

public interface DinningTableService {

    DinningTable createDinningTable(CreateDinningTableRequestDTO request);

    DinningTable getDinningTableById(String id);

    List<DinningTable> getAllDinningTables();

    DinningTable updateDinningTable(String id, UpdateDinningTableRequestDTO request);

    void deleteDinningTable(String id);

}
