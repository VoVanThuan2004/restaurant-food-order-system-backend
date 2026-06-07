package com.example.restaurant_food_system.utils;

import java.util.Set;

public class OrderItemStatus {
    public static final String NEW = "NEW";
    public static final String PENDING = "PENDING";
    public static final String ACCEPTED = "ACCEPTED";
    public static final String PREPARING = "PREPARING";
    public static final String READY = "READY";
    public static final String SERVED = "SERVED";
    public static final String CANCELLED = "CANCELLED";

    public static final Set<String> ALL_STATUSES = Set.of(
            NEW,
            PENDING,
            ACCEPTED,
            PREPARING,
            READY,
            SERVED,
            CANCELLED
    );

    public static boolean isValid(String status) {
        return ALL_STATUSES.contains(status);
    }
}
