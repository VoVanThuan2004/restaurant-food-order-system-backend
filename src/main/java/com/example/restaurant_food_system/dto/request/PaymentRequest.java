package com.example.restaurant_food_system.dto.request;

import lombok.Getter;

@Getter
public class PaymentRequest {
    private String orderId;
    private String paymentMethod;
    private Double amountReceived;
}
