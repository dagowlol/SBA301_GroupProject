package hoang.com.auction_system_be.dto.response;

import hoang.com.auction_system_be.enums.DisputeStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DisputeResponse {
    Long id;
    Long sessionId;
    Long raisedById;
    String raisedByName;
    String type;
    String description;
    DisputeStatus status;
    Long assignedManagerId;
    String resolution;
    LocalDateTime resolvedAt;
    LocalDateTime createdAt;
}
