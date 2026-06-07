package com.example.restaurant_food_system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "dish_variant_group")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DishVariantGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String groupId;

    private String groupName;
    private boolean isRequired;
    private boolean isMultiple;

    @Builder.Default
    private boolean deleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dishId")
    private Dish dish;

    @OneToMany(mappedBy = "dishVariantGroup", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DishVariantOption> variantOptions;
}
