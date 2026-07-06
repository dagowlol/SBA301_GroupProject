package hoang.com.auction_system_be.entity;

import hoang.com.auction_system_be.enums.ShippingStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Entity
@Table(name = "shippings", indexes = {
        @Index(name = "idx_shipping_item", columnList = "item_id"),
        @Index(name = "idx_shipping_buyer", columnList = "buyer_id"),
        @Index(name = "idx_shipping_tracking", columnList = "tracking_number"),
        @Index(name = "idx_shipping_status", columnList = "status")
})
@SQLDelete(sql = "UPDATE shippings SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Shipping extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    AuctionItem item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    User buyer;

    @Column(name = "tracking_number", length = 100)
    String trackingNumber;

    @Column(name = "carrier", length = 50)
    String carrier;

    @Column(name = "shipping_fee", precision = 18, scale = 2)
    BigDecimal shippingFee;

    @Column(name = "shipping_address", columnDefinition = "TEXT")
    String shippingAddress;

    @Column(name = "receiver_name", length = 100)
    String receiverName;

    @Column(name = "receiver_phone", length = 20)
    String receiverPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    ShippingStatus status = ShippingStatus.PENDING;
}
