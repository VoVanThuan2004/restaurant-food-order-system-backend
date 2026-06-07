package com.example.restaurant_food_system.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dish_variant_option")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DishVariantOption {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String optionId;

    private String optionName;
    private Double priceAdjustment;

    @Builder.Default
    private boolean deleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "groupId")
    private DishVariantGroup dishVariantGroup;
}
