package hoang.com.auction_system_be.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {

    @Size(max = 100, message = "First name must be at most 100 characters")
    String firstName;

    @Size(max = 100, message = "Last name must be at most 100 characters")
    String lastName;

    @Size(max = 20, message = "Phone number must be at most 20 characters")
    String phoneNumber;

    String address;
}
