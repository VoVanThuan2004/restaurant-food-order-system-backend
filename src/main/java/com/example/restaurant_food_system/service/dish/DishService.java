package com.example.restaurant_food_system.service.dish;

import com.example.restaurant_food_system.dto.request.DishRequest;
import com.example.restaurant_food_system.dto.response.DishDetailResponse;
import com.example.restaurant_food_system.dto.response.DishResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DishService {
    void createNewDish(DishRequest dishRequest, MultipartFile file);

    Page<DishResponse> getAllDishesByCategory(String categoryId, int page, int size);

    DishDetailResponse getDishDetail(String dishId);

    void updateDish(String dishId, @Valid DishRequest dishRequest, MultipartFile file);

    void updateDishStatus(String dishId);

    void deleteDish(String dishId);

    List<DishResponse> getRecommendDishes(List<String> dishIds);
}
