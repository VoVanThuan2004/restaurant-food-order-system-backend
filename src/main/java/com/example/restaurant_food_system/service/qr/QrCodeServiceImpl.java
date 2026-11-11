package com.example.restaurant_food_system.service.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

@Service
public class QrCodeServiceImpl implements QrCodeService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String QR_CODE_PREFIX = "TBL_";
    private static final int QR_IMAGE_SIZE = 512;
    private static final int QR_MARGIN = 1;

    private final String feUrl;

    public QrCodeServiceImpl(@Value("${fe_url}") String feUrl) {
        this.feUrl = normalizeBaseUrl(feUrl);
    }

    @Override
    public String generateCode() {
        byte[] randomBytes = new byte[16];
        SECURE_RANDOM.nextBytes(randomBytes);
        return QR_CODE_PREFIX + Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    @Override
    public String buildQrUrl(String qrCode) {
        return UriComponentsBuilder.fromUriString(feUrl)
                .path("/order")
                .queryParam("table", qrCode)
                .build()
                .toUriString();
    }

    @Override
    public byte[] renderPng(String content) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = Map.of(
                    EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name(),
                    EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M,
                    EncodeHintType.MARGIN, QR_MARGIN
            );

            BitMatrix bitMatrix = qrCodeWriter.encode(
                    content,
                    BarcodeFormat.QR_CODE,
                    QR_IMAGE_SIZE,
                    QR_IMAGE_SIZE,
                    hints
            );

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                MatrixToImageWriter.writeToStream(
                        bitMatrix,
                        "PNG",
                        outputStream,
                        new MatrixToImageConfig()
                );
                return outputStream.toByteArray();
            }
        } catch (WriterException | IOException e) {
            throw new IllegalStateException("Không thể tạo ảnh QR", e);
        }
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("Chưa cấu hình fe_url");
        }

        return baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
    }
}
