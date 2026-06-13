package hoang.com.auction_system_be.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "item_images")
@SQLDelete(sql = "UPDATE item_images SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemImage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    AuctionItem item;

    @Column(name = "image_url", nullable = false, length = 500)
    String imageUrl;

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    boolean isPrimary = false;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    int sortOrder = 0;
}
