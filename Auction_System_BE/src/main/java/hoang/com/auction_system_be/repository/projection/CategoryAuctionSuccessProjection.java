package hoang.com.auction_system_be.repository.projection;

public interface CategoryAuctionSuccessProjection {
    Long getCategoryId();
    String getCategoryName();
    Long getSuccessfulAuctions();
}
