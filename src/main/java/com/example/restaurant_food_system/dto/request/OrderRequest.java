package com.example.restaurant_food_system.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class OrderRequest {
    @NotBlank(message = "Người tạo bàn không hợp lệ")
    private String userId;  // Ai tạo bàn

    @NotBlank(message = "Bàn ăn không hợp lệ")
    private String diningTableId;
}
