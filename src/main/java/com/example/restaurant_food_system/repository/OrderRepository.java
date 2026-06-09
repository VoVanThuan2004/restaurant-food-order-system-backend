package com.example.restaurant_food_system.repository;

import com.example.restaurant_food_system.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDate;

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
}
