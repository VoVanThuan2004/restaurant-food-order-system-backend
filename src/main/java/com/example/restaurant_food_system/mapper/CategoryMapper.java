package com.example.restaurant_food_system.mapper;

import com.example.restaurant_food_system.dto.request.CategoryRequest;
import com.example.restaurant_food_system.dto.response.CategoryResponse;
import com.example.restaurant_food_system.entity.Category;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CategoryMapper {

    public CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .categoryId(category.getCategoryId())
                .categoryName(category.getCategoryName())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    public List<CategoryResponse> mapToResponseList(List<Category> categoryList) {
        return categoryList.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Category mapToEntity(CategoryRequest categoryRequest) {
        return Category.builder()
                .categoryName(categoryRequest.getCategoryName())
                .build();
    }
}
