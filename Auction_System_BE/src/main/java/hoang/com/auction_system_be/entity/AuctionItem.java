package hoang.com.auction_system_be.entity;

import hoang.com.auction_system_be.enums.ItemStatus;
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
@Table(name = "auction_items", indexes = {
        @Index(name = "idx_item_seller", columnList = "seller_id"),
        @Index(name = "idx_item_category", columnList = "category_id"),
        @Index(name = "idx_item_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuctionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 200)
    String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    User seller;

    @Column(name = "starting_price", nullable = false, precision = 18, scale = 2)
    BigDecimal startingPrice;

    @Column(name = "reserve_price", precision = 18, scale = 2)
    BigDecimal reservePrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    ItemStatus status = ItemStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    User reviewedBy;

    @Column(name = "reviewed_at")
    LocalDateTime reviewedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    String rejectionReason;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    List<ItemImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY)
    @Builder.Default
    List<AuctionSession> sessions = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}
