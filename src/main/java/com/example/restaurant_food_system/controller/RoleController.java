package com.example.restaurant_food_system.controller;

import com.example.restaurant_food_system.dto.response.ApiResponse;
import com.example.restaurant_food_system.dto.response.RoleOptionResponse;
import com.example.restaurant_food_system.service.role.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/roles")
public class RoleController {
    private final RoleService roleService;

    // Api lấy danh sách lựa chọn vai trò
    @GetMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RoleOptionResponse>>> getAllRoleOptions() {
        List<RoleOptionResponse> roles = roleService.getAllRoleOptions();

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.<List<RoleOptionResponse>>builder()
                        .status("success")
                        .code(HttpStatus.OK.value())
                        .message("Lấy danh sách lựa chọn vai trò thành công")
                        .data(roles)
                .build());
    }
}
