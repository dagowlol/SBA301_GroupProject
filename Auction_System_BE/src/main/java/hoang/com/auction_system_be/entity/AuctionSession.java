package hoang.com.auction_system_be.entity;

import hoang.com.auction_system_be.enums.SessionStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "auction_sessions", indexes = {
        @Index(name = "idx_session_item", columnList = "item_id"),
        @Index(name = "idx_session_status", columnList = "status"),
        @Index(name = "idx_session_time", columnList = "start_time, end_time")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuctionSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    AuctionItem item;

    @Column(name = "start_time", nullable = false)
    LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    LocalDateTime endTime;

    @Column(name = "reserve_price", precision = 18, scale = 2)
    BigDecimal reservePrice;

    @Column(name = "minimum_increment", nullable = false, precision = 18, scale = 2)
    BigDecimal minimumIncrement;

    @Column(name = "current_highest_bid", precision = 18, scale = 2)
    BigDecimal currentHighestBid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_winner_participant_id")
    AuctionParticipant currentWinnerParticipant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    SessionStatus status = SessionStatus.SCHEDULED;

    @Column(name = "anti_snipe_window_seconds", nullable = false)
    @Builder.Default
    Integer antiSnipeWindowSeconds = 10;

    @Column(name = "anti_snipe_extension_seconds", nullable = false)
    @Builder.Default
    Integer antiSnipeExtensionSeconds = 30;

    @Column(name = "bid_count", nullable = false)
    @Builder.Default
    Integer bidCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    User createdBy;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    String cancellationReason;

    @OneToMany(mappedBy = "session", fetch = FetchType.LAZY)
    @Builder.Default
    List<AuctionParticipant> participants = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}

