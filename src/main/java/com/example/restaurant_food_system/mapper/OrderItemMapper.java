package com.example.restaurant_food_system.mapper;

import com.example.restaurant_food_system.dto.response.OrderItemResponse;
import com.example.restaurant_food_system.entity.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderItemMapper {
    private final OrderItemVariantMapper orderItemVariantMapper;

    public OrderItemResponse mapToResponse(OrderItem orderItem) {
        return OrderItemResponse.builder()
                .orderItemId(orderItem.getOrderItemId())
                .dishName(orderItem.getDish().getName())
                .dishImage(orderItem.getDishImage())
                .basePrice(orderItem.getBasePrice())
                .quantity(orderItem.getQuantity())
                .notes(orderItem.getNotes())
                .currentStatus(orderItem.getCurrentStatus())
                .totalPrice(orderItem.getTotalPrice())
                .orderItemVariants(orderItem.getOrderItemVariants().stream()
                        .map(orderItemVariantMapper::mapToResponse)
                        .toList()
                )
                .build();
    }
}
