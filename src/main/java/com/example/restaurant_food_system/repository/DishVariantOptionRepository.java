package com.example.restaurant_food_system.repository;

import com.example.restaurant_food_system.entity.DishVariantOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DishVariantOptionRepository extends JpaRepository<DishVariantOption, String> {

    List<DishVariantOption> findAllByDishVariantGroup_GroupIdAndDeletedFalse(String groupId);
}
