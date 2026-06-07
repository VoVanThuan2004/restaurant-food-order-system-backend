package com.example.restaurant_food_system.mapper;

import com.example.restaurant_food_system.dto.response.OrderItemVariantResponse;
import com.example.restaurant_food_system.entity.OrderItemVariant;
import org.springframework.stereotype.Component;

@Component
public class OrderItemVariantMapper {

    public OrderItemVariantResponse mapToResponse(OrderItemVariant orderItemVariant) {
        return OrderItemVariantResponse.builder()
                .groupId(orderItemVariant.getDishVariantGroup().getGroupId())
                .groupName(orderItemVariant.getDishVariantGroup().getGroupName())
                .optionId(orderItemVariant.getDishVariantOption().getOptionId())
                .optionName(orderItemVariant.getDishVariantOption().getOptionName())
                .priceAdjustment(orderItemVariant.getPriceAdjustment())
                .build();
    }
}
