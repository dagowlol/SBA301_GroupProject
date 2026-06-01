package hoang.com.auction_system_be.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "auction_extension_logs",
        indexes = {
                @Index(name = "idx_extlog_session", columnList = "session_id"),
                @Index(name = "idx_extlog_created_at", columnList = "created_at")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuctionExtensionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    AuctionSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "triggered_by_participant_id")
    AuctionParticipant triggeredByParticipant;

    @Column(name = "window_seconds")
    Integer windowSeconds;

    @Column(name = "extension_seconds")
    Integer extensionSeconds;

    @Column(name = "old_end_time", nullable = false)
    LocalDateTime oldEndTime;

    @Column(name = "new_end_time", nullable = false)
    LocalDateTime newEndTime;

    @Column(name = "reason", length = 200)
    String reason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;
}

