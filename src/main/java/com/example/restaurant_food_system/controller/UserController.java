package com.example.restaurant_food_system.controller;

import com.example.restaurant_food_system.dto.request.UserCreateRequest;
import com.example.restaurant_food_system.dto.request.UserRequest;
import com.example.restaurant_food_system.dto.response.ApiResponse;
import com.example.restaurant_food_system.dto.response.UserProfileResponse;
import com.example.restaurant_food_system.dto.response.UserResponse;
import com.example.restaurant_food_system.security.CustomUserDetail;
import com.example.restaurant_food_system.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile() {
        CustomUserDetail customUserDetail = (CustomUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = customUserDetail.getUserId();

        UserProfileResponse userProfileResponse = userService.getProfile(userId);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.<UserProfileResponse>builder()
                        .status("success")
                        .code(HttpStatus.OK.value())
                        .message("Lấy thông tin profile thành công")
                        .data(userProfileResponse)
                .build());
    }

    @PutMapping(
            value = "/{userId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateUser(
            @PathVariable String userId,
            @Valid @RequestPart("data") UserRequest userRequest,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        UserProfileResponse userProfileResponse = userService.updateUser(userId, userRequest, file);

        return ResponseEntity.ok(
                ApiResponse.<UserProfileResponse>builder()
                        .status("success")
                        .code(HttpStatus.OK.value())
                        .message("Cập nhật thông tin thành công")
                        .data(userProfileResponse)
                        .build()
        );
    }

    // Cập nhật thông tin người dùng dành cho admin
    @PutMapping("/{userId}/admin")
    @PreAuthorize("hasAuthority('USER_EDIT')")
    public ResponseEntity<ApiResponse<?>> updateUserForAdmin(
            @PathVariable String userId,
            @Valid @RequestBody UserCreateRequest userUpdateRequest
    ) {
        userService.updateUserForAdmin(userId, userUpdateRequest);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status("success")
                        .code(HttpStatus.OK.value())
                        .message("Cập nhật thông tin thành công")
                        .build()
        );
    }

    // Lấy danh sách người dùng (Admin)
    @GetMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search
    ) {
        Page<UserResponse> userResponses = userService.getAllUsers(page, size, search);

        return ResponseEntity.ok(
                ApiResponse.<Page<UserResponse>>builder()
                        .status("success")
                        .code(HttpStatus.OK.value())
                        .message("Lấy danh sách người dùng")
                        .data(userResponses)
                        .build()
        );
    }

    // Tạo người dùng
    @PostMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> createUser(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        userService.createUser(userCreateRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.builder()
                        .status("success")
                        .code(HttpStatus.CREATED.value())
                        .message("Tạo người dùng thành công")
                .build());
    }

    // Khóa - mở khóa tài khoản
    @PutMapping("/{userId}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> activateUser(@PathVariable String userId) {

        boolean isActive = userService.activateUser(userId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .message(isActive ? "Mở khóa tài khoản thành công" : "Khóa tài khoản thành công")
                .build());
    }
}
