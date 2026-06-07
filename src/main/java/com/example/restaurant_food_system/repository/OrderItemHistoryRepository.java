package com.example.restaurant_food_system.repository;

import com.example.restaurant_food_system.entity.OrderItemHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemHistoryRepository extends JpaRepository<OrderItemHistory, String> {
}
