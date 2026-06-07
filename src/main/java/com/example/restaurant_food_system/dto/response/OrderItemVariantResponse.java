package com.example.restaurant_food_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemVariantResponse {
    private String groupId;
    private String groupName;
    private String optionId;
    private String optionName;
    private Double priceAdjustment;
}
