package com.example.restaurant_food_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String userId;
    private String fullName;
    private String avatarUrl;
    private List<String> roles;
    private String accessToken;
}
