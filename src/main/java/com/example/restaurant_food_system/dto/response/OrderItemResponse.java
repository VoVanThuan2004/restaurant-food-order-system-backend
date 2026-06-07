package com.example.restaurant_food_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemResponse {
    private String orderItemId;
    private String dishName;
    private Double basePrice;
    private Integer quantity;
    private String dishImage;
    private String notes;
    private String currentStatus;
    private Double totalPrice;
    List<OrderItemVariantResponse> orderItemVariants;
}
