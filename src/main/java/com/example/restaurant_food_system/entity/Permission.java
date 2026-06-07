package com.example.restaurant_food_system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "permission")
@AllArgsConstructor @NoArgsConstructor
@Builder @Getter @Setter
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private String permissionId;

    @Column(unique = true, nullable = false)
    private String name;

    private String resource;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
