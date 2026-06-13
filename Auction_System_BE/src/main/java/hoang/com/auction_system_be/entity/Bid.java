package hoang.com.auction_system_be.entity;

import hoang.com.auction_system_be.enums.BidStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bids", indexes = {
        @Index(name = "idx_bid_participant", columnList = "participant_id"),
        @Index(name = "idx_bid_timestamp", columnList = "bid_timestamp"),
        @Index(name = "idx_bid_status", columnList = "status")
})
@SQLDelete(sql = "UPDATE bids SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Bid extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participant_id", nullable = false)
    AuctionParticipant participant;

    @Column(nullable = false, precision = 18, scale = 2)
    BigDecimal amount;

    @Column(name = "is_auto_bid", nullable = false)
    @Builder.Default
    boolean isAutoBid = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    BidStatus status = BidStatus.ACTIVE;

    @Column(name = "bid_timestamp", nullable = false)
    LocalDateTime bidTimestamp;

    @Column(name = "ip_address", length = 50)
    String ipAddress;
}
