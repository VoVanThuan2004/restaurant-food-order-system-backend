package com.example.restaurant_food_system.mapper;

import com.example.restaurant_food_system.dto.response.DishDetailResponse;
import com.example.restaurant_food_system.dto.response.DishResponse;
import com.example.restaurant_food_system.dto.response.DishVariantGroupResponse;
import com.example.restaurant_food_system.dto.response.DishVariantOptionResponse;
import com.example.restaurant_food_system.entity.Dish;
import org.springframework.stereotype.Component;

@Component
public class DishMapper {

    public DishResponse mapToResponse(Dish dish) {
        return DishResponse.builder()
                .dishId(dish.getDishId())
                .name(dish.getName())
                .basePrice(dish.getBasePrice())
                .image(dish.getImage())
                .status(dish.isStatus())
                .build();
    }

    public DishDetailResponse mapToResponseDetail(Dish dish) {
        return DishDetailResponse.builder()
                .categoryId(dish.getCategory().getCategoryId())
                .dishId(dish.getDishId())
                .name(dish.getName())
                .basePrice(dish.getBasePrice())
                .image(dish.getImage())
                .status(dish.isStatus())
                .variants(dish.getVariantGroups().stream()
                        .filter(dishVariantGroup -> !dishVariantGroup.isDeleted())
                        .map(dishVariantGroup -> DishVariantGroupResponse.builder()
                                .groupId(dishVariantGroup.getGroupId())
                                .groupName(dishVariantGroup.getGroupName())
                                .isRequired(dishVariantGroup.isRequired())
                                .isMultiple(dishVariantGroup.isMultiple())
                                .options(dishVariantGroup.getVariantOptions().stream()
                                        .filter(dishVariantOption -> !dishVariantOption.isDeleted())
                                        .map(dishVariantOption -> DishVariantOptionResponse.builder()
                                                .optionId(dishVariantOption.getOptionId())
                                                .optionName(dishVariantOption.getOptionName())
                                                .priceAdjustment(dishVariantOption.getPriceAdjustment())
                                                .build())
                                        .toList()
                                )
                                .build())
                        .toList()
                )
                .build();
    }
}
