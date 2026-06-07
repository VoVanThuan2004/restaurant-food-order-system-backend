package com.example.restaurant_food_system.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_item_variant")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class OrderItemVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String orderItemVariantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderItemId")
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "groupId")
    private DishVariantGroup dishVariantGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "optionId")
    private DishVariantOption dishVariantOption;

    private Double priceAdjustment;
}
