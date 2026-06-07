package com.example.restaurant_food_system.controller;

import com.example.restaurant_food_system.dto.request.CategoryRequest;
import com.example.restaurant_food_system.dto.response.ApiResponse;
import com.example.restaurant_food_system.dto.response.CategoryResponse;
import com.example.restaurant_food_system.service.category.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryRequest request
    ) {

        categoryService.createCategory(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.<CategoryResponse>builder()
                                .status("success")
                                .code(HttpStatus.CREATED.value())
                                .message("Tạo danh mục thành công")
                                .build()
                );
    }

    @PutMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable String categoryId,
            @Valid @RequestBody CategoryRequest request
    ) {

        categoryService.updateCategory(categoryId, request);

        return ResponseEntity.ok(
                ApiResponse.<CategoryResponse>builder()
                        .status("success")
                        .code(HttpStatus.OK.value())
                        .message("Cập nhật danh mục thành công")
                        .build()
        );
    }

    @GetMapping("")
    @PreAuthorize("hasAuthority('CATEGORY_VIEW')")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {

        List<CategoryResponse> response = categoryService.getCategories();

        return ResponseEntity.ok(
                ApiResponse.<List<CategoryResponse>>builder()
                        .status("success")
                        .code(HttpStatus.OK.value())
                        .message("Lấy danh sách danh mục thành công")
                        .data(response)
                        .build()
        );
    }
}
