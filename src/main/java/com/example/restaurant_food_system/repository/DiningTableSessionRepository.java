package com.example.restaurant_food_system.repository;

import com.example.restaurant_food_system.entity.DiningTableSession;
import com.example.restaurant_food_system.utils.DiningTableSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiningTableSessionRepository extends JpaRepository<DiningTableSession, String> {
    boolean existsByDiningTable_DiningTableIdAndStatus(
            String diningTableId,
            DiningTableSessionStatus status
    );
}
