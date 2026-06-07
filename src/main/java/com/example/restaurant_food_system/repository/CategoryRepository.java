package com.example.restaurant_food_system.repository;

import com.example.restaurant_food_system.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, String> {

    boolean existsCategoriesByCategoryName(String categoryName);
}
