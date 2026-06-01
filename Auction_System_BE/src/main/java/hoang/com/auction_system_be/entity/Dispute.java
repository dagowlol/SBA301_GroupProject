package hoang.com.auction_system_be.entity;

import hoang.com.auction_system_be.enums.DisputeStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "disputes", indexes = {
        @Index(name = "idx_dispute_session", columnList = "session_id"),
        @Index(name = "idx_dispute_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Dispute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    AuctionSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raised_by_id", nullable = false)
    User raisedBy;

    @Column(nullable = false, length = 100)
    String type;

    @Column(nullable = false, columnDefinition = "TEXT")
    String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    DisputeStatus status = DisputeStatus.OPEN;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_manager_id")
    User assignedManager;

    @Column(columnDefinition = "TEXT")
    String resolution;

    @Column(name = "resolved_at")
    LocalDateTime resolvedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}

