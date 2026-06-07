package com.example.restaurant_food_system.utils;

import com.example.restaurant_food_system.security.TokenPayload;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;

@Component
public class JwtTokenUtil {
    @Value("${secret_jwt}")
    private String secret;

    public String generateToken(TokenPayload tokenPayload, long expiredDate) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("payload", tokenPayload);
        return Jwts.builder().setClaims(claims).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiredDate * 1000))
                .signWith(SignatureAlgorithm.HS256, secret).compact();
    }

    public TokenPayload getTokenPayload(String token) {
        return getClaimsFromToken(token, (Claims claims) -> {
            Map<String, Object> payload = (Map<String, Object>) claims.get("payload");
            return TokenPayload.builder()
                    .userId((String) payload.get("userId"))
                    .fullName((String) payload.get("fullName"))
                    .roles((List<String>) payload.get("roles"))
                    .build();
        });
    }

    public <T> T getClaimsFromToken(String token, Function<Claims, T> claimResolver) {
        final Claims claims = Jwts.parser().setSigningKey(secret)
                .parseClaimsJws(token).getBody();

        return claimResolver.apply(claims);
    }

    public boolean isValidToken(String token, TokenPayload tokenPayload) {
        if (isTokenExpired(token)) {
            return false;
        }
        TokenPayload payload = getTokenPayload(token);
        return Objects.equals(tokenPayload.getUserId(), payload.getUserId())
                && tokenPayload.getFullName().equals(payload.getFullName());
    }

    public boolean isTokenExpired(String token) {
        Date expiredTime = getClaimsFromToken(token, Claims::getExpiration);
        return expiredTime != null && new Date().after(expiredTime);
    }
}
