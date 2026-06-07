package com.example.restaurant_food_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class DishDetailResponse {
    private String categoryId;
    private String dishId;
    private String name;
    private Double basePrice;
    private String image;
    private boolean status;
    private List<DishVariantGroupResponse> variants;
}
