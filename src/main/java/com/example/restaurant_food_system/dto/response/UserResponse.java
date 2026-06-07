package com.example.restaurant_food_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor @Builder
public class UserResponse {
    private String userId;
    private String email;
    private String fullName;
    private Integer gender;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String avatarUrl;
    private List<RoleOptionResponse> roles;
    private boolean isActive;
}
