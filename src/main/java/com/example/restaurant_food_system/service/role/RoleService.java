package com.example.restaurant_food_system.service.role;

import com.example.restaurant_food_system.dto.response.RoleOptionResponse;

import java.util.List;

public interface RoleService {
    List<RoleOptionResponse> getAllRoleOptions();
}
