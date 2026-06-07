package com.example.restaurant_food_system.dto.response;

import lombok.*;

import java.time.Instant;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemHistoryResponse {
    private String status;
    private Instant createdAt;
}
