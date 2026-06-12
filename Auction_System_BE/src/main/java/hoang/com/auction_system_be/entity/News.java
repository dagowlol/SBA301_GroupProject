package hoang.com.auction_system_be.entity;

import hoang.com.auction_system_be.enums.NewsStatus;
import hoang.com.auction_system_be.enums.NewsType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "news", indexes = {
        @Index(name = "idx_news_type", columnList = "type"),
        @Index(name = "idx_news_status", columnList = "status")
})
@SQLDelete(sql = "UPDATE news SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class News extends BaseEntity {

    @Column(nullable = false, length = 300)
    String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    String content;

    @Column(name = "thumbnail_url", length = 500)
    String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    NewsType type = NewsType.NEWS;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    NewsStatus status = NewsStatus.ENABLED;

    @Column(name = "is_featured", nullable = false)
    @Builder.Default
    boolean isFeatured = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    User author;

    @Column(name = "published_at")
    LocalDateTime publishedAt;
}
