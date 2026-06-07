package com.example.restaurant_food_system.service.diningTable;

import com.example.restaurant_food_system.dto.request.DiningTableRequest;
import com.example.restaurant_food_system.dto.request.UpdateTablePositionRequest;
import com.example.restaurant_food_system.dto.response.DiningTableResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;

import java.util.List;

public interface DiningTableService {
    void createDiningTable(@Valid DiningTableRequest diningTableRequest);

    void updateDiningTable(String diningTableId, @Valid DiningTableRequest request);

    boolean disableDiningTable(String diningTableId);

    Page<DiningTableResponse> getAllTables(int page, int size, String search, Boolean status);

    void deleteDiningTable(String diningTableId);

    DiningTableResponse getDiningTableDetail(String diningTableId);

    void reIndexAllTables();

    List<DiningTableResponse> updateDiningTablePosition(String diningTableId, UpdateTablePositionRequest updateTablePositionRequest);
}
