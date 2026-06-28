package hoang.com.auction_system_be.service.item;

import hoang.com.auction_system_be.dto.request.ItemRejectRequest;
import hoang.com.auction_system_be.dto.request.ItemRequest;
import hoang.com.auction_system_be.dto.response.ItemResponse;
import hoang.com.auction_system_be.dto.response.PageResponse;
import hoang.com.auction_system_be.enums.ItemStatus;

public interface AuctionItemService {

    ItemResponse createItem(ItemRequest request);

    ItemResponse approveItem(Long itemId);

    ItemResponse rejectItem(Long itemId, ItemRejectRequest request);

    PageResponse<ItemResponse> getItems(int page, int size, String name, Long categoryId, ItemStatus status);
}
