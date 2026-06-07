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
public class OrderResponse {
    private String orderId;
    private String diningTableName;
    private String staffName;
    private Double totalPrice;
    private Double amountReceived;
    private Double changeAmount;
    List<OrderItemResponse> orderItems;
}
