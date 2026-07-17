package hoang.com.auction_system_be.dto.request;

import hoang.com.auction_system_be.enums.ItemStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateItemRequest {
    String name;
    String description;
    Long categoryId;
    BigDecimal startingPrice;
    BigDecimal reservePrice;
    ItemStatus status;
    String imageUrl;
    LocalDateTime startTime;
    LocalDateTime endTime;
    String artist;
    String submittedBy;
}
