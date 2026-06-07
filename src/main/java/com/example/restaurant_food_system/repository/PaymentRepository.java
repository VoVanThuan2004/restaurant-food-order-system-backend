package com.example.restaurant_food_system.repository;

import com.example.restaurant_food_system.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, String> {
}
