package com.example.restaurant_food_system.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor @NoArgsConstructor
public class TokenPayload {
    private String userId;
    private String fullName;
    private List<String> roles;
}
