package com.example.restaurant_food_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class DishResponse {
    private String dishId;
    private String name;
    private Double basePrice;
    private String image;
    private boolean status;
}
