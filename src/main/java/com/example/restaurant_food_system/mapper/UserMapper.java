package com.example.restaurant_food_system.mapper;

import com.example.restaurant_food_system.dto.response.RoleOptionResponse;
import com.example.restaurant_food_system.dto.response.UserProfileResponse;
import com.example.restaurant_food_system.dto.response.UserResponse;
import com.example.restaurant_food_system.entity.Role;
import com.example.restaurant_food_system.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper {

    public UserProfileResponse mapToProfileResponse(User user) {
        return UserProfileResponse.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .gender(user.getGender())
                .dateOfBirth(user.getDateOfBirth())
                .phoneNumber(user.getPhoneNumber())
                .avatarUrl(user.getAvatarUrl())
                .roles(user.getRoles().stream()
                        .map(Role::getRoleName)
                        .toList()
                )
                .build();
    }

    public UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .gender(user.getGender())
                .dateOfBirth(user.getDateOfBirth())
                .avatarUrl(user.getAvatarUrl())
                .roles(user.getRoles().stream()
                        .map(role -> RoleOptionResponse.builder()
                                .roleId(role.getRoleId())
                                .roleName(role.getRoleName())
                                .build())
                        .toList()
                )
                .isActive(user.isActive())
                .build();
    }

    public List<UserResponse> mapToResponseList(List<User> users) {
        return users.stream()
                .map(this::mapToResponse)
                .toList();
    }
}
