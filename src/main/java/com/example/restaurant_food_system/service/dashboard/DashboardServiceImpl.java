package com.example.restaurant_food_system.service.dashboard;

import com.example.restaurant_food_system.dto.response.RevenueStatisticResponse;
import com.example.restaurant_food_system.dto.response.TodayStatisticDTO;
import com.example.restaurant_food_system.exception.BadRequestException;
import com.example.restaurant_food_system.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    private final OrderRepository orderRepository;

    @Override
    public TodayStatisticDTO getTodayStatistic() {
        Instant now = Instant.now();

        // 1. Lấy tổng số đơn hàng hôm nay
        Long totalOrders = orderRepository.countTotalOrdersToday(now);

        // 2. Lấy tổng doanh thu hôm nay
        Double totalRevenue = orderRepository.sumTotalRevenueToday(now);

        return TodayStatisticDTO.builder()
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue == null ? 0 : totalRevenue)
                .build();
    }

    @Override
    public List<RevenueStatisticResponse> getRevenueStatistic(LocalDate startDate, LocalDate endDate, String type) {
        Instant startInstant = null;
        Instant endInstant = null;
        ZoneId zoneId = ZoneId.systemDefault();

        if (startDate != null && endDate != null) {
            startInstant = startDate
                    .atStartOfDay(zoneId)
                    .toInstant();

            endInstant = endDate
                    .plusDays(1)
                    .atStartOfDay(zoneId)
                    .toInstant();

        } else {
            LocalDate today = LocalDate.now();

            startInstant = today
                    .minusDays(7)
                    .atStartOfDay(zoneId)
                    .toInstant();

            endInstant = today
                    .plusDays(1)
                    .atStartOfDay(zoneId)
                    .toInstant();
        }

        // Query data trả về

        switch (type) {
            case "YEAR":
                List<Object[]> rowsYear = orderRepository.statisticRevenueByYear(startInstant, endInstant);

                return rowsYear.stream()
                        .map(row -> RevenueStatisticResponse.builder()
                                .label(String.valueOf(((Number) row[0]).intValue()))
                                .revenue(((Number) row[1]).doubleValue())
                                .build())
                        .toList();
            case "MONTH":
                return orderRepository.statisticRevenueByMonth(startInstant, endInstant);
            case "QUARTER":
                List<Object[]> rowsQuarter = orderRepository.statisticRevenueByQuarter(startInstant, endInstant);

                return rowsQuarter.stream()
                        .map(row -> {
                            Integer year = ((Number) row[0]).intValue();
                            Integer quarter = ((Number) row[1]).intValue();

                            return RevenueStatisticResponse.builder()
                                    .label(year + "-Q" + quarter)
                                    .revenue(((Number) row[2]).doubleValue())
                                    .build();
                        })
                        .toList();
            case "WEEK":
                List<Object[]> rowsWeek = orderRepository.statisticRevenueByWeek(startInstant, endInstant);


                return rowsWeek.stream()
                        .map(row -> RevenueStatisticResponse.builder()
                                .label(String.valueOf(((Number) row[0]).intValue()))
                                .revenue(((Number) row[1]).doubleValue())
                                .build())
                        .toList();
            default:
                 throw new BadRequestException("Loại thống kê không hợp lệ");
        }
    }
}
