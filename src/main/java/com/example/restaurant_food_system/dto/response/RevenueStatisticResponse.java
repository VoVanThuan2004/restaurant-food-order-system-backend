package com.example.restaurant_food_system.dto.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class RevenueStatisticResponse {
    private String label;
    private Double revenue;
}
