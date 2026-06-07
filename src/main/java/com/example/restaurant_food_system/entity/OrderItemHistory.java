package com.example.restaurant_food_system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;


@Entity
@Table(name = "order_item_history")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OrderItemHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String orderItemHistoryId;

    private String status;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderItemId")
    private OrderItem orderItem;
}
