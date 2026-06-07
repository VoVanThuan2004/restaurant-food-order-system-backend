package com.example.restaurant_food_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class UserProfileResponse {
    private String userId;
    private String fullName;
    private String phoneNumber;
    private Integer gender;
    private LocalDate dateOfBirth;
    private String avatarUrl;
    private List<String> roles;
}
