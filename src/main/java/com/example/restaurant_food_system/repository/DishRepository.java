package com.example.restaurant_food_system.repository;

import com.example.restaurant_food_system.entity.Dish;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DishRepository extends JpaRepository<Dish, String> {

    @Query("""
        select d
        from Dish d
        where (:categoryId is null or d.category.categoryId = :categoryId)
        and d.deleted = false
    """)
    Page<Dish> findAllDishesByCategory(@Param("categoryId") String categoryId, Pageable pageable);


}
