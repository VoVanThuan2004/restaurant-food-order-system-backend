package com.example.restaurant_food_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DishStatusResponse {
    private String dishId;
    private boolean status;
}
