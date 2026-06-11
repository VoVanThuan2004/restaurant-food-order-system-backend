package com.example.restaurant_food_system.dto.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class TodayStatisticDTO {
    private Long totalOrders;
    private Double totalRevenue;
}
