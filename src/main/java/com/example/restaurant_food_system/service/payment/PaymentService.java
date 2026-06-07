package com.example.restaurant_food_system.service.payment;

import com.example.restaurant_food_system.dto.request.PaymentRequest;

public interface PaymentService {
    void payOrder(PaymentRequest paymentRequest);

    String getPaymentDetailsByOrder(String orderId);
}
