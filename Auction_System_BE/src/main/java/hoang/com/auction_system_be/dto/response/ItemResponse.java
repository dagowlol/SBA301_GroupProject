package hoang.com.auction_system_be.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemResponse {
    Long id;
    String name;
    String description;
    Long categoryId;
    String categoryName;
    Long sellerId;
    String sellerName;
    BigDecimal startingPrice;
    BigDecimal reservePrice;
    ItemStatus status;
    Long reviewedById;
    String reviewedByName;
    LocalDateTime reviewedAt;
    String rejectionReason;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
