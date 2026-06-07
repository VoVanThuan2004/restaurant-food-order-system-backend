package com.example.restaurant_food_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@AllArgsConstructor
@Builder
@Getter @Setter
public class DiningTableResponse {
    private String diningTableId;
    private String name;
    private Integer capacity;
    private Boolean status;
    private Instant createdAt;
    private Instant updatedAt;
}
