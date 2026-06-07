package hoang.com.auction_system_be.entity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
@Entity
@Table(name = "otp_records", indexes = {
        @Index(name = "idx_otp_email", columnList = "email"),
        @Index(name = "idx_otp_email_code", columnList = "email, otp_code")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OtpRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(nullable = false, length = 255)
    String email;
    @Column(name = "otp_code", nullable = false, length = 6)
    String otpCode;
    @Column(name = "expiry_time", nullable = false)
    LocalDateTime expiryTime;
    @Column(nullable = false)
    @Builder.Default
    boolean used = false;
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;
}
