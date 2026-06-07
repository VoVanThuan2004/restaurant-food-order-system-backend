package com.example.restaurant_food_system.repository;

import com.example.restaurant_food_system.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, String> {
    @Query("""
        SELECT COALESCE(SUM(oi.totalPrice), 0)
        FROM OrderItem oi
        WHERE oi.order.orderId = :orderId AND oi.currentStatus <> 'CANCELLED'
    """)
    Double sumTotalPriceByOrderId(@Param("orderId") String orderId);


    // Lấy danh sách items thuộc order có trạng thái NEW
    @Query("""
        SELECT oi
        FROM OrderItem oi
        WHERE oi.order.orderId = :orderId AND oi.currentStatus = :currentStatus
    """)
    List<OrderItem> findAllByOrderIdAndCurrentStatus(
            @Param("orderId") String orderId,
            @Param("currentStatus") String currentStatus
    );

    @Query("""
        SELECT COUNT(oi)
        FROM OrderItem oi
        WHERE oi.order.orderId = :orderId AND oi.currentStatus <> 'CANCELLED'
    """)
    Integer countAllByOrderId(String orderId);
}
