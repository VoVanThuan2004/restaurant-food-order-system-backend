package com.example.restaurant_food_system.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class OrderItemVariantRequest {
    @NotBlank(message = "Mã lựa chọn biến thể không được để trống")
    private String optionId;
}
