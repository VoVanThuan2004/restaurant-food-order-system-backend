package com.example.restaurant_food_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
@Builder
public class CategoryResponse {
    private String categoryId;
    private String categoryName;
    private Instant createdAt;
    private Instant updatedAt;
}
