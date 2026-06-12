package com.example.restaurant_food_system.repository;

import com.example.restaurant_food_system.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, String> {

    boolean existsCategoriesByCategoryName(String categoryName);

    @Query("""
        select c
        from Category c
        where c.deleted = false
        order by c.createdAt desc
    """)
    List<Category> findAllCategories();
}
