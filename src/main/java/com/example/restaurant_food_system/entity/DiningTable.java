package com.example.restaurant_food_system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;

@Entity
@Table(name = "dining_table")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class DiningTable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String diningTableId;

    @Column(unique = true, nullable = false)
    private String name;

    private Integer capacity;

    @Column(nullable = false)
    private Long position;

    @Builder.Default
    private Boolean status = Boolean.TRUE;

    @Builder.Default
    private Boolean deleted = Boolean.FALSE;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
