package com.example.restaurant_food_system.repository;

import com.example.restaurant_food_system.dto.response.RevenueStatisticResponse;
import com.example.restaurant_food_system.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, String> {

    @Query("""
        select o
        from Order o
        where o.diningTable.diningTableId = :diningTableId
        and o.user.userId = :userId
        and o.status = false
    """)
    Order findByDiningTableIdAndUserId(
            @Param("diningTableId") String diningTableId,
            @Param("userId") String userId);


    // Lấy danh sách orders chưa thanh toán
    Page<Order> findOrdersByStatusFalse(Pageable pageable);


    @Query("""
        SELECT o
        FROM Order o
        JOIN Payment p on p.order.orderId = o.orderId
        WHERE o.user.userId = :staffId AND o.status = true AND p.paymentStatus = 'SUCCESS'
        ORDER BY p.paidAt DESC
    """)
    Page<Order> findOrdersByStaffAndPaid(String staffId, Pageable pageable);

    @Query("""
        SELECT o
        FROM Order o
        JOIN Payment p on p.order.orderId = o.orderId
        WHERE (:userId is null or o.user.userId = :userId)
        AND ((:startDate is null and :endDate is null) or p.paidAt BETWEEN :startDate AND :endDate)
        AND (:status is null or o.status = :status)
    """)
    Page<Order> findOrdersForAdmin(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            @Param("userId") String userId,
            @Param("status") Boolean status,
            Pageable pageable);


   // ======== THỐNG KÊ ========
    @Query("""
        select count(o)
        from Order o
        where o.status = true and o.createdAt >= :now and o.createdAt < :now
    """)
    Long countTotalOrdersToday(@Param("now") Instant now);

    @Query("""
        select sum(o.totalPrice)
        from Order o
        where o.status = true and o.createdAt >= :now and o.createdAt < :now
    """)
    Double sumTotalRevenueToday(@Param("now") Instant now);

    @Query(value = """
        SELECT
            DATE_FORMAT(o.created_at, '%Y-%m') as label,
            SUM(o.total_price) as revenue
        FROM orders o
        WHERE o.status = true
              AND o.created_at >= :startDate
              AND o.created_at < :endDate
        GROUP BY DATE_FORMAT(o.created_at, '%Y-%m')
        ORDER BY label
    """, nativeQuery = true)
    List<RevenueStatisticResponse> statisticRevenueByMonth(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    @Query(value = """
        SELECT
            YEAR(o.created_at) as label,
            SUM(o.total_price) as revenue
        FROM orders o
        WHERE o.status = true
              AND o.created_at >= :startDate
              AND o.created_at < :endDate
        GROUP BY YEAR(o.created_at)
        ORDER BY label
    """, nativeQuery = true)
    List<Object[]> statisticRevenueByYear(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    @Query(value = """
        SELECT
            YEAR(o.created_at) as yearValue,
            QUARTER(o.created_at) as quarterValue,
            SUM(o.total_price) as revenue
        FROM orders o
        WHERE o.status = true
          AND o.created_at >= :startDate
          AND o.created_at < :endDate
        GROUP BY
            YEAR(o.created_at),
            QUARTER(o.created_at)
        ORDER BY
            YEAR(o.created_at),
            QUARTER(o.created_at)
    """, nativeQuery = true)
    List<Object[]> statisticRevenueByQuarter(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );


    @Query(value = """
        SELECT
            YEARWEEK(o.created_at, 1) as label,
            SUM(o.total_price) as revenue
        FROM orders o
        WHERE o.status = true
          AND o.created_at >= :startDate
          AND o.created_at < :endDate
        GROUP BY YEARWEEK(o.created_at, 1)
        ORDER BY YEARWEEK(o.created_at, 1)
    """, nativeQuery = true)
    List<Object[]> statisticRevenueByWeek(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );
}
