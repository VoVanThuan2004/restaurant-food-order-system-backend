package com.example.restaurant_food_system.controller;

import com.example.restaurant_food_system.dto.request.DishRequest;
import com.example.restaurant_food_system.dto.response.ApiResponse;
import com.example.restaurant_food_system.dto.response.DishDetailResponse;
import com.example.restaurant_food_system.dto.response.DishResponse;
import com.example.restaurant_food_system.service.dish.DishService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/dishes")
public class DishController {
    private final DishService dishService;

    // Tạo món ăn mới
    @PostMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> createNewDish(
            @Valid @RequestPart("data") DishRequest dishRequest,
            @RequestPart("file") MultipartFile file
    ) {
        dishService.createNewDish(dishRequest, file);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.builder()
                        .status("success")
                        .code(HttpStatus.CREATED.value())
                        .message("Tạo món ăn mới thành công")
                .build());
    }

    // Cập nhật thông tin món ăn
    @PutMapping("/{dishId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> updateDish(
            @PathVariable String dishId,
            @Valid @RequestPart("data") DishRequest dishRequest,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        dishService.updateDish(dishId, dishRequest, file);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .message("Cập nhật món ăn thành công")
                .build());
    }

    // Lấy danh sách món ăn thuộc danh mục
    @GetMapping("")
    public ResponseEntity<ApiResponse<Page<DishResponse>>> getAllDishesByCategory(
            @RequestParam(required = false) String categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<DishResponse> dishResponses = dishService.getAllDishesByCategory(categoryId, page, size);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.<Page<DishResponse>>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách món ăn thành công")
                .data(dishResponses)
                .build());
    }

    // Lấy chi tiết món ăn
    @GetMapping("/{dishId}")
    public ResponseEntity<ApiResponse<DishDetailResponse>> getDishDetail(@PathVariable String dishId) {
        DishDetailResponse dishDetailResponse = dishService.getDishDetail(dishId);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.<DishDetailResponse>builder()
                        .status("success")
                        .code(HttpStatus.OK.value())
                        .message("Lấy chi tiết món ăn thành công")
                        .data(dishDetailResponse)
                .build());
    }

    // Tắt - bật món ăn
    @PutMapping("/{dishId}/status")
    @PreAuthorize("hasAuthority('DISH_EDIT')")
    public ResponseEntity<ApiResponse<?>> updateDishStatus(@PathVariable String dishId) {
        dishService.updateDishStatus(dishId);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .message("Cập nhật trạng thái món ăn thành công")
                .build());
    }

    // Xóa món ăn
    @DeleteMapping("/{dishId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> deleteDish(@PathVariable String dishId) {
        dishService.deleteDish(dishId);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .message("Xóa món ăn thành công")
                .build());
    }
    
    // Lấy danh sách món ăn gợi ý
    @GetMapping("/recommend")
    public ResponseEntity<ApiResponse<List<DishResponse>>> getRecommendDishes(
            @RequestParam List<String> dishIds
    ) {
        List<DishResponse> dishResponses = dishService.getRecommendDishes(dishIds);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.<List<DishResponse>>builder()
                        .status("success")
                        .code(HttpStatus.OK.value())
                        .message("Lấy danh sách các món ăn gợi ý")
                        .data(dishResponses)
                .build());
    }
}
