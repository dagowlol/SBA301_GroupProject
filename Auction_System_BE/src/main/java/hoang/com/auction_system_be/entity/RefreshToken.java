package hoang.com.auction_system_be.entity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import java.time.Instant;

@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_token", columnList = "token", unique = true),
        @Index(name = "idx_refresh_user_email", columnList = "user_email")
})
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RefreshToken extends BaseEntity {
    @Column(nullable = false, length = 1000)
    String token;
    @Column(name = "user_email", nullable = false, length = 255)
    String userEmail;
    @Column(name = "expiry_time", nullable = false)
    Instant expiryTime;
    @Column(nullable = false)
    @Builder.Default
    boolean revoked = false;
}
