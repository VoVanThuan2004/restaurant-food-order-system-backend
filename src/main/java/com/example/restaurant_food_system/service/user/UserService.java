package com.example.restaurant_food_system.service.user;

import com.example.restaurant_food_system.dto.request.UserCreateRequest;
import com.example.restaurant_food_system.dto.request.UserRequest;
import com.example.restaurant_food_system.dto.response.UserProfileResponse;
import com.example.restaurant_food_system.dto.response.UserResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserProfileResponse getProfile(String userId);

    void updateUser(String userId, UserRequest userRequest, MultipartFile file);

    Page<UserResponse> getAllUsers(int page, int size, String search);

    void createUser(@Valid UserCreateRequest userCreateRequest);

    boolean activateUser(String userId);

    void updateUserForAdmin(String userId, @Valid UserCreateRequest userUpdateRequest);
}
