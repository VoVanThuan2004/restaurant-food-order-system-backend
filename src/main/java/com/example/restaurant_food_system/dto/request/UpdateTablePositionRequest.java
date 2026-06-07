package com.example.restaurant_food_system.dto.request;

import lombok.Getter;

@Getter
public class UpdateTablePositionRequest {
    private String previousTableId;
    private String nextTableId;
}
