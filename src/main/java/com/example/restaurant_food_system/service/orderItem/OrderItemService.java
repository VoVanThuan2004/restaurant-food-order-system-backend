package com.example.restaurant_food_system.service.orderItem;

import com.example.restaurant_food_system.dto.request.OrderItemRequest;
import com.example.restaurant_food_system.dto.response.OrderItemHistoryResponse;
import com.example.restaurant_food_system.utils.OrderItemStatus;
import jakarta.validation.Valid;

import java.util.List;

public interface OrderItemService {
    void addOrderItem(@Valid OrderItemRequest orderItemRequest);

    void deleteOrderItem(String orderItemId);

    void updateOrderItemQuantity(String orderItemId, Integer newQuantity);

    void updateOrderItemNotes(String orderItemId, String notes);

    void confirmStatusItem(String orderItemId, String status);

    List<OrderItemHistoryResponse> getOrderItemHistory(String orderItemId);
}
