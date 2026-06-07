package com.example.restaurant_food_system.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CategoryRequest {
    @NotBlank(message = "Vui lòng nhập tên danh mục")
    private String categoryName;
}
