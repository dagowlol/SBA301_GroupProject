package hoang.com.auction_system_be.entity;

import hoang.com.auction_system_be.enums.AuthProvider;
import hoang.com.auction_system_be.enums.RoleName;
import hoang.com.auction_system_be.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    String lastName;

    @Column(unique = true, nullable = false, length = 255)
    String email;

    @Column(name = "phone_number", length = 20)
    String phoneNumber;

    @Column(name = "password")
    String password;

    @Column(columnDefinition = "TEXT")
    String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", length = 20)
    AuthProvider authProvider;

    @Column(name = "provider_id")
    String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    UserStatus status = UserStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    RoleName role = RoleName.USER;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}
