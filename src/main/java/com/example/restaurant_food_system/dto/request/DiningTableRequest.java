package com.example.restaurant_food_system.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;

@Getter
public class DiningTableRequest {
    @NotBlank(message = "Tên bàn không được để trống")
    @Size(max = 50, message = "Tên bàn tối đa 50 ký tự")
    private String name;

    @NotNull(message = "Sức chứa không được để trống")
    @Min(value = 1, message = "Sức chứa phải lớn hơn 0")
    @Max(value = 50, message = "Sức chứa tối đa là 50")
    private Integer capacity;
}
