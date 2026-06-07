package com.example.restaurant_food_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class DishVariantOptionResponse {
    private String optionId;
    private String optionName;
    private Double priceAdjustment;
}
