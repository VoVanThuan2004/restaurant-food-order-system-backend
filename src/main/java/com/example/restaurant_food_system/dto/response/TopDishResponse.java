package com.example.restaurant_food_system.dto.response;

import lombok.*;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@Builder
public class TopDishResponse {
    private String dishName;
    private Long totalQuantity;
}
