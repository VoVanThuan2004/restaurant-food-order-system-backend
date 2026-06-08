package com.example.restaurant_food_system.entity;

import com.example.restaurant_food_system.utils.RuleSide;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "association_rule_item",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {
                                "rule_id",
                                "dish_id",
                                "side"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssociationRuleItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ruleItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dishId", nullable = false)
    private Dish dish;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RuleSide side;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ruleId")
    private AssociationRule rule;
}
