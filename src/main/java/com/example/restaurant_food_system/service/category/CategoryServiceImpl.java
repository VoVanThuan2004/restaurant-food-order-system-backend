package com.example.restaurant_food_system.service.category;

import com.example.restaurant_food_system.dto.request.CategoryRequest;
import com.example.restaurant_food_system.dto.response.CategoryResponse;
import com.example.restaurant_food_system.entity.Category;
import com.example.restaurant_food_system.exception.BadRequestException;
import com.example.restaurant_food_system.exception.ResourceNotFoundException;
import com.example.restaurant_food_system.mapper.CategoryMapper;
import com.example.restaurant_food_system.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;


    @Override
    public void createCategory(CategoryRequest request) {
        if (categoryRepository.existsCategoriesByCategoryName(request.getCategoryName()))
        {
            throw new BadRequestException(
                    "Tên danh mục đã tồn tại"
            );
        }

        Category category = categoryMapper.mapToEntity(request);
        categoryRepository.save(category);
    }

    @Override
    public void updateCategory(String categoryId, CategoryRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy danh mục"
                        ));

        category.setCategoryName(request.getCategoryName());
        categoryRepository.save(category);
    }

    @Override
    public List<CategoryResponse> getCategories() {
        // Query data
        List<Category> categories = categoryRepository.findAllCategories();

        // Mapping data trả về
        return categoryMapper.mapToResponseList(categories);
    }

    @Override
    public void deleteCategory(String categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy danh mục"
                        ));

        category.setDeleted(true);
        categoryRepository.save(category);
    }
}
