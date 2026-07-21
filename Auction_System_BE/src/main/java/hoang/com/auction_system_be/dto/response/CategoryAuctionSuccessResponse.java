package hoang.com.auction_system_be.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CategoryAuctionSuccessResponse {
    Long categoryId;
    String categoryName;
    long successfulAuctions;
    double percentage;
}
