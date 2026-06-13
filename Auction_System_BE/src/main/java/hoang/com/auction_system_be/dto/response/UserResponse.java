package hoang.com.auction_system_be.dto.response;

import hoang.com.auction_system_be.enums.AuthProvider;
import hoang.com.auction_system_be.enums.RoleName;
import hoang.com.auction_system_be.enums.UserStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    Long id;
    String firstName;
    String lastName;
    String email;
    String phoneNumber;
    String address;
    AuthProvider authProvider;
    UserStatus status;
    RoleName role;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
