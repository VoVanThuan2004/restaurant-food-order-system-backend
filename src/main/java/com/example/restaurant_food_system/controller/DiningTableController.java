package com.example.restaurant_food_system.controller;

import com.example.restaurant_food_system.dto.request.DiningTableRequest;
import com.example.restaurant_food_system.dto.request.UpdateTablePositionRequest;
import com.example.restaurant_food_system.dto.response.ApiResponse;
import com.example.restaurant_food_system.dto.response.DiningTableResponse;
import com.example.restaurant_food_system.service.diningTable.DiningTableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/dining-tables")
public class DiningTableController {
    private final DiningTableService diningTableService;

    @PostMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> createDiningTable(@Valid @RequestBody DiningTableRequest diningTableRequest) {
        diningTableService.createDiningTable(diningTableRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.builder()
                        .status("success")
                        .code(HttpStatus.CREATED.value())
                        .message("Tạo bàn ăn mới thành công")
                .build());
    }

    @PutMapping("/{diningTableId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> updateDiningTable(
            @PathVariable String diningTableId,
            @Valid @RequestBody DiningTableRequest request
    ) {

        diningTableService.updateDiningTable(
                        diningTableId,
                        request
        );

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status("success")
                        .code(HttpStatus.OK.value())
                        .message("Cập nhật bàn ăn thành công")
                        .build()
        );
    }

    // Cập nhật vị trí bàn ăn
    @PatchMapping("/{diningTableId}/position")
    public ResponseEntity<ApiResponse<List<DiningTableResponse>>> updateDiningTablePosition(
            @PathVariable String diningTableId,
            @RequestBody UpdateTablePositionRequest updateTablePositionRequest
    ) {

        List<DiningTableResponse> diningTableResponses = diningTableService.updateDiningTablePosition(diningTableId, updateTablePositionRequest);
        return ResponseEntity.ok(
                ApiResponse.<List<DiningTableResponse>>builder()
                        .status("success")
                        .code(HttpStatus.OK.value())
                        .message("Cập nhật vị trí bàn ăn thành công")
                        .data(diningTableResponses)
                        .build()
        );
    }

    @PatchMapping("/{diningTableId}/disable")
    public ResponseEntity<ApiResponse<?>> disableDiningTable(
            @PathVariable String diningTableId
    ) {

        boolean status = diningTableService.disableDiningTable(diningTableId);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status("success")
                        .code(HttpStatus.OK.value())
                        .message(status ? "Bật trạng thái bàn ăn thành công" : "Tắt trạng thái bàn ăn thành công")
                        .build()
        );
    }

    @DeleteMapping("/{diningTableId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> deleteDiningTable(@PathVariable String diningTableId) {
        diningTableService.deleteDiningTable(diningTableId);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status("success")
                        .code(HttpStatus.OK.value())
                        .message("Xóa bàn ăn thành công")
                        .build()
        );
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<Page<DiningTableResponse>>> getAllTables(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean status
    ) {

        Page<DiningTableResponse> diningTableResponses = diningTableService.getAllTables(page, size, search, status);

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<Page<DiningTableResponse>>builder()
                        .status("success")
                        .code(HttpStatus.OK.value())
                        .message("Lấy danh sách bàn ăn")
                        .data(diningTableResponses)
                        .build()
        );
    }

    @GetMapping("/{diningTableId}")
    public ResponseEntity<ApiResponse<DiningTableResponse>> getDiningTableDetail(
            @PathVariable String diningTableId
    ) {
        DiningTableResponse diningTableResponse = diningTableService.getDiningTableDetail(diningTableId);

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<DiningTableResponse>builder()
                        .status("success")
                        .code(HttpStatus.OK.value())
                        .message("Lấy thông tin chi tiết bàn ăn")
                        .data(diningTableResponse)
                        .build()
        );
    }
}
