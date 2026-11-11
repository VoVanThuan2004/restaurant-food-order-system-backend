package com.example.restaurant_food_system.service.qr;

public interface QrCodeService {
    String generateCode();

    String buildQrUrl(String qrCode);

    byte[] renderPng(String content);
}
