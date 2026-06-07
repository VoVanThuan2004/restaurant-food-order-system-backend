package com.example.restaurant_food_system.controller;

import com.example.restaurant_food_system.dto.request.PaymentRequest;
import com.example.restaurant_food_system.dto.response.ApiResponse;
import com.example.restaurant_food_system.service.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {
    private final PaymentService paymentService;

    // Thanh toán đơn gọi món
    @PostMapping("")
    public ResponseEntity<ApiResponse<?>> payOrder(@RequestBody PaymentRequest paymentRequest) {
        paymentService.payOrder(paymentRequest);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.builder()
                        .status("success")
                        .code(HttpStatus.OK.value())
                        .message("Thanh toán đơn gọi món thành công")
                .build());
    }
}
