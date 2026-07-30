package com.example.restaurant_food_system.repository;

import com.example.restaurant_food_system.dto.response.RevenueStatisticResponse;
import com.example.restaurant_food_system.dto.response.TopDishResponse;
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
        AND (:status is null or o.status = :status)
    """)
    Page<Order> findOrdersForAdminNoDateFilter(
            @Param("userId") String userId,
            @Param("status") Boolean status,
            Pageable pageable);

    @Query("""
        SELECT o
        FROM Order o
        JOIN Payment p on p.order.orderId = o.orderId
        WHERE (:userId is null or o.user.userId = :userId)
        AND p.paidAt BETWEEN :startDate AND :endDate
        AND (:status is null or o.status = :status)
    """)
    Page<Order> findOrdersForAdminWithDateFilter(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            @Param("userId") String userId,
            @Param("status") Boolean status,
            Pageable pageable);


   // ======== THỐNG KÊ ========
    @Query("""
        select count(o)
        from Order o
        where o.status = true and o.createdAt >= :startDate and o.createdAt < :endDate
    """)
    Long countTotalOrdersToday(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    @Query("""
        select sum(o.totalPrice)
        from Order o
        where o.status = true and o.createdAt >= :startDate and o.createdAt < :endDate
    """)
    Double sumTotalRevenueToday(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    @Query(value = """
        SELECT
            TO_CHAR(o.created_at, 'YYYY-MM') as label,
            SUM(o.total_price) as revenue
        FROM orders o
        WHERE o.status = true
              AND o.created_at >= :startDate
              AND o.created_at < :endDate
        GROUP BY TO_CHAR(o.created_at, 'YYYY-MM')
        ORDER BY label
    """, nativeQuery = true)
    List<RevenueStatisticResponse> statisticRevenueByMonth(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    @Query(value = """
        SELECT
            EXTRACT(YEAR FROM o.created_at) as label,
            SUM(o.total_price) as revenue
        FROM orders o
        WHERE o.status = true
              AND o.created_at >= :startDate
              AND o.created_at < :endDate
        GROUP BY EXTRACT(YEAR FROM o.created_at)
        ORDER BY label
    """, nativeQuery = true)
    List<Object[]> statisticRevenueByYear(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    @Query(value = """
        SELECT
            EXTRACT(YEAR FROM o.created_at) as yearValue,
            EXTRACT(QUARTER FROM o.created_at) as quarterValue,
            SUM(o.total_price) as revenue
        FROM orders o
        WHERE o.status = true
          AND o.created_at >= :startDate
          AND o.created_at < :endDate
        GROUP BY
            EXTRACT(YEAR FROM o.created_at),
            EXTRACT(QUARTER FROM o.created_at)
        ORDER BY
            EXTRACT(YEAR FROM o.created_at),
            EXTRACT(QUARTER FROM o.created_at)
    """, nativeQuery = true)
    List<Object[]> statisticRevenueByQuarter(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );


    @Query(value = """
        SELECT
            TO_CHAR(o.created_at, 'IYYY-IW') as label,
            SUM(o.total_price) as revenue
        FROM orders o
        WHERE o.status = true
          AND o.created_at >= :startDate
          AND o.created_at < :endDate
        GROUP BY TO_CHAR(o.created_at, 'IYYY-IW')
        ORDER BY TO_CHAR(o.created_at, 'IYYY-IW')
    """, nativeQuery = true)
    List<Object[]> statisticRevenueByWeek(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    @Query("""
        select d.name, sum(oi.quantity) as quantity
        from Order o
        join OrderItem oi on oi.order.orderId = o.orderId
        join Dish d on d.dishId = oi.dish.dishId
        where o.status = true and o.createdAt between :startDate and :endDate
        group by d.dishId, d.name
        order by quantity desc
    """)
    List<Object[]> statisticTopDishes(
            @Param("startDate")Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable
    );
}
