package com.example.restaurant_food_system.service.order;

import com.example.restaurant_food_system.dto.request.OrderRequest;
import com.example.restaurant_food_system.dto.response.OrderPaymentResponse;
import com.example.restaurant_food_system.dto.response.OrderResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;

import java.time.LocalDate;

public interface OrderService {
    String createOrder(@Valid OrderRequest orderRequest);

    String checkOrder(String diningTableId, String userId);

    OrderResponse getOrderDetail(String orderId);

    Page<OrderResponse> getOrdersByChef(int page, int size);

    void placeOrder(String orderId);

    Page<OrderPaymentResponse> getOrdersByStaff(String staffId, int page, int size);

    Integer getOrderTotalItems(String orderId);

    Page<OrderResponse> getOrdersByAdmin(int page, int size, LocalDate startDate, LocalDate endDate, String userId, Boolean status);
}
