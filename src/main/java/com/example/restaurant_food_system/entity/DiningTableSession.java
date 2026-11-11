package com.example.restaurant_food_system.entity;

import com.example.restaurant_food_system.utils.DiningTableSessionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "dining_table_session",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_table_session_token_hash",
                        columnNames = "session_token_hash"
                )
        },
        indexes = {
                @Index(
                        name = "idx_table_session_table_status",
                        columnList = "dining_table_id, status"
                ),
                @Index(
                        name = "idx_table_session_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiningTableSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dining_table_id", nullable = false)
    private DiningTable diningTable;

    @Column(name = "session_token_hash", nullable = false, length = 64)
    private String sessionTokenHash;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DiningTableSessionStatus status = DiningTableSessionStatus.OPEN;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opened_by_user_id")
    private User openedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "closed_by_user_id")
    private User closedBy;

    @CreationTimestamp
    @Column(name = "opened_at", nullable = false, updatable = false)
    private Instant openedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @UpdateTimestamp
    private Instant updatedAt;

    @Version
    private Long version;
}
