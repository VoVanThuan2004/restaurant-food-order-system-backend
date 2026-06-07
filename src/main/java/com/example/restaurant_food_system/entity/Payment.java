package com.example.restaurant_food_system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "payment")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter @Setter
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String paymentId;

    private Double amountPaid;

    private Double amountReceived;

    private Double changeAmount;

    private String paymentMethod;

    private String paymentStatus;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant paidAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId", unique = true)
    private Order order;
}
