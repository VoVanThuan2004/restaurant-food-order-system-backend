package com.example.restaurant_food_system.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class OrderItemRequest {
    @NotBlank(message = "Mã đơn gọi món không được để trống")
    private String orderId;

    @NotBlank(message = "Mã món ăn không được để trống")
    private String dishId;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer quantity;

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String notes;

    @Valid
    private List<OrderItemVariantRequest> variants;
}
