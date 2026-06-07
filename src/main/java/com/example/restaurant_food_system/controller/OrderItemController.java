package com.example.restaurant_food_system.controller;

import com.example.restaurant_food_system.dto.request.OrderItemRequest;
import com.example.restaurant_food_system.dto.response.ApiResponse;
import com.example.restaurant_food_system.dto.response.OrderItemHistoryResponse;
import com.example.restaurant_food_system.service.orderItem.OrderItemService;
import com.example.restaurant_food_system.utils.OrderItemStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/order-items")
public class OrderItemController {
    private final OrderItemService orderItemService;

    // Thêm món ăn vào đơn gọi món
    @PostMapping("")
    @PreAuthorize("hasAuthority('ORDER_UPDATE')")
    public ResponseEntity<ApiResponse<?>> addOrderItem(@Valid @RequestBody OrderItemRequest orderItemRequest) {
        orderItemService.addOrderItem(orderItemRequest);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.builder()
                        .status("success")
                        .code(HttpStatus.OK.value())
                        .message("Đã thêm món ăn vào đơn gọi món")
                .build());
    }

    // Xóa món ăn ra khỏi đơn gọi món
    @DeleteMapping("/{orderItemId}")
    public ResponseEntity<ApiResponse<?>> deleteOrderItem(@PathVariable String orderItemId) {
        orderItemService.deleteOrderItem(orderItemId);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .message("Xóa món ăn ra khỏi đơn gọi món")
                .build());
    }

    // Cập nhật số lượng món ăn
    @PutMapping("/{orderItemId}/quantity")
    public ResponseEntity<ApiResponse<?>> updateOrderItemQuantity(
            @PathVariable String orderItemId,
            @RequestParam Integer newQuantity
    ) {
        orderItemService.updateOrderItemQuantity(orderItemId, newQuantity);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .message("Cập nhật số lượng món ăn trong đơn gọi món")
                .build());
    }

    // Cập nhật ghi chú món ăn
    @PutMapping("/{orderItemId}/notes")
    public ResponseEntity<ApiResponse<?>> updateOrderItemNotes(
            @PathVariable String orderItemId,
            @RequestParam String notes
    ) {
        orderItemService.updateOrderItemNotes(orderItemId, notes);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .message("Cập nhật ghi chú món ăn trong đơn gọi món")
                .build());
    }

    // Xác nhận trạng thái món ăn
    @PutMapping("/{orderItemId}/confirm")
    public ResponseEntity<ApiResponse<?>> confirmStatusItem(
            @PathVariable String orderItemId,
            @RequestParam String status
    ) {
        orderItemService.confirmStatusItem(orderItemId, status);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .message("Xác nhận trạng thái món ăn thành công")
                .build());
    }

    // Xem chi tiết lịch sử của order item
    @GetMapping("/{orderItemId}/history")
    public ResponseEntity<ApiResponse<List<OrderItemHistoryResponse>>> getOrderItemHistory(@PathVariable String orderItemId) {
        List<OrderItemHistoryResponse> orderItemHistoryResponses = orderItemService.getOrderItemHistory(orderItemId);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.<List<OrderItemHistoryResponse>>builder()
                        .status("success")
                        .code(HttpStatus.OK.value())
                        .message("Lấy danh sách lịch sử trạng thái món ăn thành công")
                        .data(orderItemHistoryResponses)
                .build());
    }

}
