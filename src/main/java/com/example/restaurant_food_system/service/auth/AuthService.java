package com.example.restaurant_food_system.service.auth;

import com.example.restaurant_food_system.dto.request.ChangePasswordRequest;
import com.example.restaurant_food_system.dto.request.ForgotPasswordDTO;
import com.example.restaurant_food_system.dto.request.LoginRequest;
import com.example.restaurant_food_system.dto.request.ResetPasswordDTO;
import com.example.restaurant_food_system.dto.response.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

public interface AuthService {
    LoginResponse login(@Valid LoginRequest loginRequest, HttpServletRequest httpServletRequest,
                        HttpServletResponse httpServletResponse);

    void logout(String refreshToken, HttpServletResponse response);

    void changePassword(String userId, ChangePasswordRequest changePasswordRequest);

    void forgotPassword(@Valid ForgotPasswordDTO forgotPasswordDTO, HttpServletRequest request);

    void resetPassword(@Valid ResetPasswordDTO resetPasswordDTO);
}
