package com.example.restaurant_food_system.service.category;

import com.example.restaurant_food_system.dto.request.CategoryRequest;
import com.example.restaurant_food_system.dto.response.CategoryResponse;
import jakarta.validation.Valid;

import java.util.List;

public interface CategoryService {
    void createCategory(@Valid CategoryRequest request);

    void updateCategory(String categoryId, @Valid CategoryRequest request);

    List<CategoryResponse> getCategories();
}
