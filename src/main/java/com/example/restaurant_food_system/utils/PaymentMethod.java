package com.example.restaurant_food_system.utils;

import java.util.Set;

public class PaymentMethod {
    public static final String CASH = "CASH";
    public static final String TRANSFER = "TRANSFER";

    private static final Set<String> paymentMethods = Set.of(
            CASH, TRANSFER
    );

    public static boolean isValidPaymentMethod (String paymentMethod) {
        return paymentMethods.contains(paymentMethod);
    }
}
