package hoang.com.auction_system_be.entity;

import hoang.com.auction_system_be.enums.AuthProvider;
import hoang.com.auction_system_be.enums.RoleName;
import hoang.com.auction_system_be.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email", unique = true)
})
@SQLDelete(sql = "UPDATE users SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User extends BaseEntity {

    @Column(name = "first_name", nullable = false, length = 100)
    String firstName;
    @Column(name = "last_name", nullable = false, length = 100)
    String lastName;
    @Column(unique = true, nullable = false, length = 255)
    String email;
    @Column(name = "password_hash")
    String passwordHash;
    @Column(name = "phone_number", length = 20)
    String phoneNumber;

    @Column(columnDefinition = "TEXT")
    String address;
    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", length = 20)
    @Builder.Default
    AuthProvider authProvider = AuthProvider.LOCAL;
    @Column(name = "provider_id")
    String providerId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    UserStatus status = UserStatus.PENDING;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    RoleName role = RoleName.USER;
    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    Integer failedLoginAttempts = 0;
    @Column(name = "locked_at")
    Instant lockedAt;
}
