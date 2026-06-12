package hoang.com.auction_system_be.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "auction_extension_logs",
        indexes = {
                @Index(name = "idx_extlog_session", columnList = "session_id"),
                @Index(name = "idx_extlog_created_at", columnList = "created_at")
        }
)
@SQLDelete(sql = "UPDATE auction_extension_logs SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuctionExtensionLog extends BaseEntity {

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
}
