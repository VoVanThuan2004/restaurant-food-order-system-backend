package com.example.restaurant_food_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderPaymentResponse {
    private String orderId;
    private String diningTableName;
    private String staffName;
    private Double totalPrice;
    private Double amountReceived;
    private Double changeAmount;
    private String paymentMethod;
    private Instant paidAt;
}
