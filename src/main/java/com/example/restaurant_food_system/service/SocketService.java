package com.example.restaurant_food_system.service;

import com.example.restaurant_food_system.dto.response.DiningTableResponse;
import com.example.restaurant_food_system.dto.response.DishStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class SocketService {
    private final SimpMessagingTemplate messagingTemplate;

    // Hàm gửi thông báo tắt bật trạng thái món ăn
    public void sendDishStatus(DishStatusResponse dishStatusResponse) {
        messagingTemplate.convertAndSend("/topic/dish-status", dishStatusResponse);
    }

    // Hàm gửi cập nhật trạng thái bàn ăn
    public void sendDiningTableStatus(DiningTableResponse diningTableResponse) {
        messagingTemplate.convertAndSend("/topic/dining-table-status", diningTableResponse);
    }

    // Hàm gửi thông tin đặt món ăn
    public void sendPlaceOrder(String orderId) {
        messagingTemplate.convertAndSend("/topic/place-order", orderId);
    }

    // Hàm gửi cập nhật trạng thái món ăn
    public void sendConfirmStatusItem(
            String orderId,
            String orderItemId,
            String currentStatus
    ) {
        Map<String, Object> payload = Map.of(
                "orderItemId", orderItemId,
                "currentStatus", currentStatus
        );

        messagingTemplate.convertAndSend("/topic/confirm-item/" + orderId, Optional.of(payload));
    }
}
