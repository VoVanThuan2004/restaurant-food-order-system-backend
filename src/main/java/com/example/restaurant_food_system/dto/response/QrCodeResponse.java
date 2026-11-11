package com.example.restaurant_food_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class QrCodeResponse {
    private String diningTableId;
    private String tableName;
    private String qrCode;
    private String qrUrl;
    private Instant qrGeneratedAt;
}
