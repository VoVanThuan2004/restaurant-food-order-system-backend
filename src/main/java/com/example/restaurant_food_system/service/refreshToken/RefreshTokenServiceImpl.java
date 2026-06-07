package com.example.restaurant_food_system.service.refreshToken;

import com.example.restaurant_food_system.entity.RefreshToken;
import com.example.restaurant_food_system.exception.BadRequestException;
import com.example.restaurant_food_system.exception.ResourceNotFoundException;
import com.example.restaurant_food_system.repository.RefreshTokenRepository;
import com.example.restaurant_food_system.security.TokenPayload;
import com.example.restaurant_food_system.utils.JwtTokenUtil;
import com.example.restaurant_food_system.utils.TokenConstant;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenUtil jwtTokenUtil;


    @Override
    public String refreshTokenForUser(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new BadRequestException("Refresh token không hợp lệ");
        }

        // 1. Kiểm tra mã token còn hạn hay không
        if (jwtTokenUtil.isTokenExpired(refreshToken)) {
            throw new BadRequestException("Refresh token đã hết hạn");
        }

        // 2. Kiểm tra mã token hiện tại có tồn tại
        Optional<RefreshToken> refreshTokenOptional = refreshTokenRepository.findByRefreshToken(DigestUtils.sha256Hex(refreshToken));
        if (refreshTokenOptional.isEmpty()) {
            throw new ResourceNotFoundException("Mã refresh token không hợp lệ");
        }

        TokenPayload tokenPayload = jwtTokenUtil.getTokenPayload(refreshToken);

        // 3. Tạo mã access token
        String accessToken = jwtTokenUtil.generateToken(TokenPayload.builder()
                .userId(tokenPayload.getUserId())
                .fullName(tokenPayload.getFullName())
                .roles(tokenPayload.getRoles())
                .build(), TokenConstant.ACCESS_TOKEN_EXPIRATION);

        return accessToken;
    }
}
