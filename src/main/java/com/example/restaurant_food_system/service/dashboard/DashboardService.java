package com.example.restaurant_food_system.service.dashboard;

import com.example.restaurant_food_system.dto.response.RevenueStatisticResponse;
import com.example.restaurant_food_system.dto.response.TodayStatisticDTO;
import com.example.restaurant_food_system.dto.response.TopDishResponse;

import java.time.LocalDate;
import java.util.List;

public interface DashboardService {
    TodayStatisticDTO getTodayStatistic();

    List<RevenueStatisticResponse> getRevenueStatistic(LocalDate startDate, LocalDate endDate, String type);

    List<TopDishResponse> getTopDishes(Integer limit, LocalDate startDate, LocalDate endDate);
}
