package com.example.restaurant_food_system.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UpdateDishRequest {
    @NotBlank(message = "Danh mục không được để trống")
    private String categoryId;

    @NotBlank(message = "Tên món ăn không được để trống")
    private String name;

    @NotNull(message = "Giá cơ bản không được để trống")
    @Min(value = 0, message = "Giá món ăn phải lớn hơn hoặc bằng 0")
    private Double basePrice;


}
