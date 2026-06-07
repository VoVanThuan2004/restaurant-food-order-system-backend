package com.example.restaurant_food_system.mapper;

import com.example.restaurant_food_system.dto.request.DiningTableRequest;
import com.example.restaurant_food_system.dto.response.DiningTableResponse;
import com.example.restaurant_food_system.entity.DiningTable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DiningTableMapper {

    public DiningTableResponse mapToResponse(DiningTable diningTable) {
        return DiningTableResponse.builder()
                .diningTableId(diningTable.getDiningTableId())
                .name(diningTable.getName())
                .capacity(diningTable.getCapacity())
                .status(diningTable.getStatus())
                .createdAt(diningTable.getCreatedAt())
                .updatedAt(diningTable.getUpdatedAt())
                .build();
    }

    public List<DiningTableResponse> mapToResponseList(List<DiningTable> diningTableList) {
        return diningTableList.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public DiningTable mapToEntity(DiningTableRequest diningTableRequest) {
        return DiningTable.builder()
                .name(diningTableRequest.getName())
                .capacity(diningTableRequest.getCapacity())
                .build();
    }
}
