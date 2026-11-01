package com.example.restaurant_food_system.service.auth;

import com.example.restaurant_food_system.dto.request.ChangePasswordRequest;
import com.example.restaurant_food_system.dto.request.CustomerRegisterRequest;
import com.example.restaurant_food_system.dto.request.ForgotPasswordDTO;
import com.example.restaurant_food_system.dto.request.LoginRequest;
import com.example.restaurant_food_system.dto.request.ResendVerificationRequest;
import com.example.restaurant_food_system.dto.request.ResetPasswordDTO;
import com.example.restaurant_food_system.dto.request.VerifyEmailRequest;
import com.example.restaurant_food_system.dto.response.LoginResponse;
import com.example.restaurant_food_system.dto.response.RegisterResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

public interface AuthService {
    LoginResponse login(@Valid LoginRequest loginRequest, HttpServletRequest httpServletRequest,
                        HttpServletResponse httpServletResponse);

    RegisterResponse register(@Valid CustomerRegisterRequest customerRegisterRequest,
                              HttpServletRequest httpServletRequest);

    void verifyEmail(@Valid VerifyEmailRequest verifyEmailRequest);

    void resendVerification(@Valid ResendVerificationRequest resendVerificationRequest,
                            HttpServletRequest httpServletRequest);

    void logout(String refreshToken, HttpServletResponse response);

    void changePassword(String userId, ChangePasswordRequest changePasswordRequest);

    void forgotPassword(@Valid ForgotPasswordDTO forgotPasswordDTO, HttpServletRequest request);

    void resetPassword(@Valid ResetPasswordDTO resetPasswordDTO);
}
