package com.example.restaurant_food_system.controller;

import com.example.restaurant_food_system.dto.response.ApiResponse;
import com.example.restaurant_food_system.dto.response.RevenueStatisticResponse;
import com.example.restaurant_food_system.dto.response.TodayStatisticDTO;
import com.example.restaurant_food_system.dto.response.TopDishResponse;
import com.example.restaurant_food_system.entity.Dish;
import com.example.restaurant_food_system.service.dashboard.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/dashboards")
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/today")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TodayStatisticDTO>> getTodayStatistic() {
        TodayStatisticDTO todayStatisticDTO = dashboardService.getTodayStatistic();

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.<TodayStatisticDTO>builder()
                        .status("success")
                        .code(HttpStatus.OK.value())
                        .message("Thống kê hôm nay")
                        .data(todayStatisticDTO)
                .build());
    }

    // Thống kê doanh thu theo thời gian
    @GetMapping("/statistics/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RevenueStatisticResponse>>> getRevenueStatistic(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam() String type
            ) {
        List<RevenueStatisticResponse> revenueStatisticResponses = dashboardService.getRevenueStatistic(startDate, endDate, type);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.<List<RevenueStatisticResponse>>builder()
                        .status("success")
                        .code(HttpStatus.OK.value())
                        .message("Thống kê doanh thu theo thời gian")
                        .data(revenueStatisticResponses)
                .build());
    }

    // Thống kê top các loại món ăn bán chạy theo thời gian
    @GetMapping("/top-dishes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<TopDishResponse>>> getTopDishes(
            @RequestParam(defaultValue = "5") Integer limit,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {
        List<TopDishResponse> topDishResponses = dashboardService.getTopDishes(limit, startDate, endDate);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.<List<TopDishResponse>>builder()
                        .status("success")
                        .code(HttpStatus.OK.value())
                        .message("Lấy top các loại món ăn bán chạy")
                        .data(topDishResponses)
                .build());
    }
}
