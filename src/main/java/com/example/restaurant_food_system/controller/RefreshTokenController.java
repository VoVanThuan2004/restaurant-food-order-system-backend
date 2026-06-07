package com.example.restaurant_food_system.controller;

import com.example.restaurant_food_system.dto.response.ApiResponse;
import com.example.restaurant_food_system.service.refreshToken.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/")
public class RefreshTokenController {
    private final RefreshTokenService refreshTokenService;

    @PostMapping("refresh-token")
    public ResponseEntity<ApiResponse<?>> refreshTokenForUser(
            @CookieValue(value = "refreshToken", required = false) String refreshToken
    ) {
        String accessToken = refreshTokenService.refreshTokenForUser(refreshToken);
        return ResponseEntity.ok(ApiResponse.builder()
                        .status("success")
                        .code(HttpStatus.OK.value())
                        .message("Refresh token thành công")
                        .data(accessToken)
                .build());
    }
}
