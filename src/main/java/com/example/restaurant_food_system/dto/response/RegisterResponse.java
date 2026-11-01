package com.example.restaurant_food_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponse {
    private String userId;
    private String email;
    private String fullName;
    private List<String> roles;
}
