package hoang.com.auction_system_be.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Entity
@Table(name = "auto_bid_configs")
@SQLDelete(sql = "UPDATE auto_bid_configs SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AutoBidConfig extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participant_id", nullable = false, unique = true)
    AuctionParticipant participant;

    @Column(name = "max_bid_amount", nullable = false, precision = 18, scale = 2)
    BigDecimal maxBidAmount;

    @Column(name = "bid_increment", precision = 18, scale = 2)
    BigDecimal bidIncrement;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    boolean isActive = true;
}
