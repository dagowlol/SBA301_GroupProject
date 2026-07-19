package hoang.com.auction_system_be.mapper;

import hoang.com.auction_system_be.dto.response.ItemResponse;
import hoang.com.auction_system_be.entity.AuctionItem;
import org.springframework.stereotype.Component;
import hoang.com.auction_system_be.service.storage.ObjectStorageService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ItemMapper {
    private final ObjectStorageService objectStorageService;

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

        if (item.getSessions() != null && !item.getSessions().isEmpty()) {
            builder.sessionId(item.getSessions().get(0).getId());
        }

        if (item.getImages() != null && !item.getImages().isEmpty()) {
            item.getImages().stream()
                    .filter(img -> img.isPrimary())
                    .findFirst()
                    .ifPresentOrElse(
                            img -> builder.imageUrl(objectStorageService.resolveUrl(img.getImageUrl())),
                            () -> builder.imageUrl(objectStorageService.resolveUrl(item.getImages().get(0).getImageUrl()))
                    );
        }

        return builder.build();
    }
}

