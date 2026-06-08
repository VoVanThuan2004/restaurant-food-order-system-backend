package com.example.restaurant_food_system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "association_rule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssociationRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ruleId;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal support;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal confidence;

    @Column(nullable = false)
    private Instant createdAt;

    @OneToMany(
            mappedBy = "rule",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<AssociationRuleItem> items = new ArrayList<>();
}
