package com.example.restaurant_food_system.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.util.List;

@Getter
public class UpdateDishVariantGroupRequest {
    private String groupId;

    @NotBlank(message = "Tên nhóm biến thể không được để trống")
    private String groupName;

    private boolean required;

    private boolean multiple;

    @Valid
    @NotEmpty(message = "Danh sách lựa chọn không được để trống")
    private List<UpdateDishVariantOptionRequest> options;
}
