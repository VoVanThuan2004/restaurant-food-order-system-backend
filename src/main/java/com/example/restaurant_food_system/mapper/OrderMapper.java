package com.example.restaurant_food_system.mapper;

import com.example.restaurant_food_system.dto.response.OrderResponse;
import com.example.restaurant_food_system.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class OrderMapper {
    private final OrderItemMapper orderItemMapper;

    public OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .diningTableName(order.getDiningTable().getName())
                .staffName(order.getUser().getFullName())
                .totalPrice(order.getTotalPrice())
                .amountReceived(order.getAmountReceived())
                .changeAmount(order.getChangeAmount())
                .orderItems(order.getOrderItems().stream()
                        .map(orderItemMapper::mapToResponse)
                        .toList()
                )
                .build();
    }
}
