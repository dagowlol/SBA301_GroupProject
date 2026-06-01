package hoang.com.auction_system_be.entity;

import hoang.com.auction_system_be.enums.NewsStatus;
import hoang.com.auction_system_be.enums.NewsType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "news", indexes = {
        @Index(name = "idx_news_type", columnList = "type"),
        @Index(name = "idx_news_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

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
    Boolean isFeatured = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    User author;

    @Column(name = "published_at")
    LocalDateTime publishedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}

