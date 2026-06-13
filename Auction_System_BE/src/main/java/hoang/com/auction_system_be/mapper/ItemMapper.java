package hoang.com.auction_system_be.mapper;

import hoang.com.auction_system_be.dto.response.ItemResponse;
import hoang.com.auction_system_be.entity.AuctionItem;
import org.springframework.stereotype.Component;

@Component
public class ItemMapper {

    public ItemResponse toResponse(AuctionItem item) {
        if (item == null) {
            return null;
        }

        ItemResponse.ItemResponseBuilder builder = ItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .startingPrice(item.getStartingPrice())
                .reservePrice(item.getReservePrice())
                .status(item.getStatus())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt());

        if (item.getCategory() != null) {
            builder.categoryId(item.getCategory().getId());
            builder.categoryName(item.getCategory().getName());
        }

        if (item.getSeller() != null) {
            builder.sellerId(item.getSeller().getId());
            builder.sellerName(item.getSeller().getFirstName() + " " + item.getSeller().getLastName());
        }

        if (item.getReviewedBy() != null) {
            builder.reviewedById(item.getReviewedBy().getId());
            builder.reviewedByName(item.getReviewedBy().getFirstName() + " " + item.getReviewedBy().getLastName());
            builder.reviewedAt(item.getReviewedAt());
        }

        if (item.getRejectionReason() != null) {
            builder.rejectionReason(item.getRejectionReason());
        }

        return builder.build();
    }
}
