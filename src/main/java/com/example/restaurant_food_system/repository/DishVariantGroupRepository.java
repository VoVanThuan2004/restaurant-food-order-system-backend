package com.example.restaurant_food_system.repository;

import com.example.restaurant_food_system.entity.DishVariantGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DishVariantGroupRepository extends JpaRepository<DishVariantGroup, String> {
    List<DishVariantGroup> findAllByDish_DishIdAndDeletedFalse(String dishId);
}
