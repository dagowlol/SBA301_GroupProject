package hoang.com.auction_system_be.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "auction_participants",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_participant_user_session",
                        columnNames = {"user_id", "session_id"}
                ),
                @UniqueConstraint(
                        name = "uq_participant_session_user",
                        columnNames = {"session_id", "user_id"}
                )
        },
        indexes = {
                @Index(name = "idx_participant_session", columnList = "session_id"),
                @Index(name = "idx_participant_user", columnList = "user_id")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuctionParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    AuctionSession session;

    @OneToOne(mappedBy = "participant", fetch = FetchType.LAZY)
    AutoBidConfig autoBidConfig;

    @OneToMany(mappedBy = "participant", fetch = FetchType.LAZY)
    @Builder.Default
    List<Bid> bids = new ArrayList<>();

    @OneToMany(mappedBy = "participant", fetch = FetchType.LAZY)
    @Builder.Default
    List<Payment> payments = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, updatable = false)
    LocalDateTime joinedAt;
}

