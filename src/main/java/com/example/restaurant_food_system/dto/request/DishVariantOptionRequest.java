package com.example.restaurant_food_system.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class DishVariantOptionRequest {
    private String optionId;

    @NotBlank(message = "Tên lựa chọn không được để trống")
    private String optionName;

    @NotNull(message = "Giá cộng thêm không được để trống")
    @Min(value = 0, message = "Giá cộng thêm phải lớn hơn hoặc bằng 0")
    private Double priceAdjustment;
}
