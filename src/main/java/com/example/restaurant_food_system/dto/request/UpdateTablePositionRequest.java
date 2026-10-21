package com.example.restaurant_food_system.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTablePositionRequest {
    private String previousTableId;
    private String nextTableId;
}
