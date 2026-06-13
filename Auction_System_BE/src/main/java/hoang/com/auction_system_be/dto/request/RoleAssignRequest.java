package hoang.com.auction_system_be.dto.request;

import hoang.com.auction_system_be.enums.RoleName;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleAssignRequest {
    RoleName role;
}
