package com.example.restaurant_food_system.service;

import com.example.restaurant_food_system.dto.request.CategoryRequest;
import com.example.restaurant_food_system.dto.response.CategoryResponse;
import com.example.restaurant_food_system.entity.Category;
import com.example.restaurant_food_system.exception.BadRequestException;
import com.example.restaurant_food_system.exception.ResourceNotFoundException;
import com.example.restaurant_food_system.mapper.CategoryMapper;
import com.example.restaurant_food_system.repository.CategoryRepository;
import com.example.restaurant_food_system.service.category.CategoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private CategoryRequest buildRequest(String name) {
        CategoryRequest request = new CategoryRequest();
        request.setCategoryName(name);
        return request;
    }

    @Test
    void createCategory_WhenNameNotExists_ShouldSaveCategory() {
        CategoryRequest request = buildRequest("Đồ uống");
        Category category = Category.builder().categoryName("Đồ uống").build();

        when(categoryRepository.existsCategoriesByCategoryName("Đồ uống")).thenReturn(false);
        when(categoryMapper.mapToEntity(request)).thenReturn(category);

        categoryService.createCategory(request);

        verify(categoryRepository).save(category);
    }

    @Test
    void createCategory_WhenNameExists_ShouldThrowBadRequestException() {
        CategoryRequest request = buildRequest("Đồ uống");

        when(categoryRepository.existsCategoriesByCategoryName("Đồ uống")).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> categoryService.createCategory(request));

        assertEquals("Tên danh mục đã tồn tại", exception.getMessage());
        verify(categoryMapper, never()).mapToEntity(any());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void updateCategory_WhenFound_ShouldUpdateNameAndSave() {
        CategoryRequest request = buildRequest("Món chính");
        Category category = Category.builder().categoryId("c1").categoryName("Đồ uống").build();

        when(categoryRepository.findById("c1")).thenReturn(Optional.of(category));

        categoryService.updateCategory("c1", request);

        assertEquals("Món chính", category.getCategoryName());
        verify(categoryRepository).save(category);
    }

    @Test
    void updateCategory_WhenNotFound_ShouldThrowResourceNotFoundException() {
        CategoryRequest request = buildRequest("Món chính");

        when(categoryRepository.findById("c1")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> categoryService.updateCategory("c1", request));

        assertEquals("Không tìm thấy danh mục", exception.getMessage());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void getCategories_ShouldReturnMappedResponses() {
        List<Category> categories = List.of(
                Category.builder().categoryId("c1").categoryName("Đồ uống").build(),
                Category.builder().categoryId("c2").categoryName("Món chính").build());
        List<CategoryResponse> responses = List.of(
                CategoryResponse.builder().categoryId("c1").categoryName("Đồ uống").build(),
                CategoryResponse.builder().categoryId("c2").categoryName("Món chính").build());

        when(categoryRepository.findAllCategories()).thenReturn(categories);
        when(categoryMapper.mapToResponseList(categories)).thenReturn(responses);

        List<CategoryResponse> result = categoryService.getCategories();

        assertEquals(2, result.size());
        assertEquals("Đồ uống", result.get(0).getCategoryName());
        assertEquals("Món chính", result.get(1).getCategoryName());
        verify(categoryRepository).findAllCategories();
        verify(categoryMapper).mapToResponseList(categories);
    }

    @Test
    void getCategories_WhenEmpty_ShouldReturnEmptyList() {
        when(categoryRepository.findAllCategories()).thenReturn(List.of());
        when(categoryMapper.mapToResponseList(List.of())).thenReturn(List.of());

        List<CategoryResponse> result = categoryService.getCategories();

        assertTrue(result.isEmpty());
    }

    @Test
    void deleteCategory_WhenFound_ShouldSoftDelete() {
        Category category = Category.builder().categoryId("c1").categoryName("Đồ uống").build();

        when(categoryRepository.findById("c1")).thenReturn(Optional.of(category));

        categoryService.deleteCategory("c1");

        assertTrue(category.isDeleted());
        verify(categoryRepository).save(category);
    }

    @Test
    void deleteCategory_WhenNotFound_ShouldThrowResourceNotFoundException() {
        when(categoryRepository.findById("c1")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> categoryService.deleteCategory("c1"));

        assertEquals("Không tìm thấy danh mục", exception.getMessage());
        verify(categoryRepository, never()).save(any());
    }
}
