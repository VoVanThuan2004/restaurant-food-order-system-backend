package com.example.restaurant_food_system.controller;

import com.example.restaurant_food_system.dto.request.OrderRequest;
import com.example.restaurant_food_system.dto.response.ApiResponse;
import com.example.restaurant_food_system.dto.response.OrderPaymentResponse;
import com.example.restaurant_food_system.dto.response.OrderResponse;
import com.example.restaurant_food_system.service.order.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService orderService;

    // Tạo đơn gọi món
    @PostMapping("")
    public ResponseEntity<ApiResponse<?>> createOrder(@Valid @RequestBody OrderRequest orderRequest) {

        String orderId = orderService.createOrder(orderRequest);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.builder()
                        .status("success")
                        .code(HttpStatus.OK.value())
                        .message("Tạo đơn gọi món thành công")
                        .data(orderId)
                .build());
    }

    // Kiểm tra đơn gọi món của nhân viên
    @GetMapping("/check")
    public ResponseEntity<ApiResponse<?>> checkOrder(
            @RequestParam String diningTableId,
            @RequestParam String userId
    ) {
        String orderId = orderService.checkOrder(diningTableId, userId);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .message("Kiểm tra đơn gọi món thành công")
                .data(orderId)
                .build());
    }

    // Lấy thông tin chi tiết đơn gọi món (giỏ hàng)
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetail(@PathVariable String orderId) {
        OrderResponse orderResponse = orderService.getOrderDetail(orderId);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.<OrderResponse>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .message("Lấy chi tiết đơn gọi món thành công")
                .data(orderResponse)
                .build());
    }

    // Đặt món ăn
    @PutMapping("/{orderId}/place")
    public ResponseEntity<ApiResponse<?>> placeOrder(@PathVariable String orderId) {
        orderService.placeOrder(orderId);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .message("Gọi món ăn thành công")
                .build());
    }

    // Lấy tổng số món ăn có trong đơn gọi món
    @GetMapping("/{orderId}/total-items")
    public ResponseEntity<ApiResponse<?>> getOrderTotalItems(@PathVariable String orderId) {
        Integer totalItems = orderService.getOrderTotalItems(orderId);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.builder()
                        .status("success")
                        .code(HttpStatus.OK.value())
                        .message("Lấy tổng số món ăn có trong đơn gọi món")
                        .data(totalItems)
                .build());
    }


    // ======= Bếp =======
    @GetMapping("/chef")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getOrdersByChef(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<OrderResponse> orderResponses = orderService.getOrdersByChef(page, size);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.<Page<OrderResponse>>builder()
                        .status("success")
                        .message("Lấy danh sách đơn gọi món cho bếp")
                        .code(HttpStatus.OK.value())
                        .data(orderResponses)
                .build());
    }

    // ======= Nhân viên =======
    @GetMapping("/staff")
    public ResponseEntity<ApiResponse<Page<OrderPaymentResponse>>> getOrdersByStaff(
            @RequestParam String staffId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<OrderPaymentResponse> orderResponses = orderService.getOrdersByStaff(staffId, page, size);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.<Page<OrderPaymentResponse>>builder()
                        .status("success")
                        .code(HttpStatus.OK.value())
                        .message("Lấy danh sách đơn gọi món của nhân viên")
                        .data(orderResponses)
                .build());
    }
}
