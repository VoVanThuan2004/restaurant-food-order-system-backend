package com.example.restaurant_food_system.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class VerifyEmailRequest {
    @NotBlank(message = "Mã xác thực không hợp lệ")
    private String token;
}
