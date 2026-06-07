package com.example.restaurant_food_system.utils;

public class TokenConstant {
    // Access Token
    public static final long ACCESS_TOKEN_EXPIRATION = 30 * 60;      // 30 phút

    // Refresh Token
    public static final long REFRESH_TOKEN_EXPIRATION = 30 * 24 * 60 * 60;  // 7 ngày

    // Reset password
    public static final long RESET_TOKEN_PASSWORD_EXPIRATION = 5 * 60;
}
