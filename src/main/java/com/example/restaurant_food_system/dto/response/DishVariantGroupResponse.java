package com.example.restaurant_food_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class DishVariantGroupResponse {
    private String groupId;
    private String groupName;
    private boolean isRequired;
    private boolean isMultiple;
    private List<DishVariantOptionResponse> options;
}
