package com.example.restaurant_food_system.controller;

import com.example.restaurant_food_system.dto.request.ChangePasswordRequest;
import com.example.restaurant_food_system.dto.request.ForgotPasswordDTO;
import com.example.restaurant_food_system.dto.request.LoginRequest;
import com.example.restaurant_food_system.dto.request.ResetPasswordDTO;
import com.example.restaurant_food_system.dto.response.ApiResponse;
import com.example.restaurant_food_system.dto.response.LoginResponse;
import com.example.restaurant_food_system.security.CustomUserDetail;
import com.example.restaurant_food_system.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/")
public class AuthController {
    private final AuthService authService;

    @PostMapping("login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse
    ) {
        LoginResponse loginResponse = authService.login(loginRequest, httpServletRequest, httpServletResponse);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.<LoginResponse>builder()
                        .status("success")
                        .code(HttpStatus.OK.value())
                        .message("Đăng nhập thành công")
                        .data(loginResponse)
                .build());
    }

    @PostMapping("logout")
    public ResponseEntity<ApiResponse<?>> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        authService.logout(refreshToken, response);


        return ResponseEntity.status(HttpStatus.OK.value()).body(ApiResponse.builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .message("Đăng xuất tài khoản thành công")
                .build());
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<?>> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        CustomUserDetail customUserDetail = (CustomUserDetail) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        String userId = customUserDetail.getUserId();

        authService.changePassword(userId, changePasswordRequest);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .message("Thay đổi mật khẩu thành công")
                .build());
    }

    // Quên mật khẩu
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<?>> forgotPassword(
            @Valid @RequestBody ForgotPasswordDTO forgotPasswordDTO,
            HttpServletRequest request
    ) {
        authService.forgotPassword(forgotPasswordDTO, request);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .message("Đã gửi email gồm link reset password đến người dùng")
                .build());
    }

    // Reset mật khẩu
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<?>> resetPassword(@Valid @RequestBody ResetPasswordDTO resetPasswordDTO) {
        authService.resetPassword(resetPasswordDTO);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .message("Reset mật khẩu thành công")
                .build());
    }

}
