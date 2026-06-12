package hoang.com.auction_system_be.controller;

import hoang.com.auction_system_be.dto.request.ItemRejectRequest;
import hoang.com.auction_system_be.dto.request.ItemRequest;
import hoang.com.auction_system_be.dto.response.ApiResponse;
import hoang.com.auction_system_be.dto.response.ItemResponse;
import hoang.com.auction_system_be.dto.response.PageResponse;
import hoang.com.auction_system_be.enums.ItemStatus;
import hoang.com.auction_system_be.service.AuctionItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Auction Item Controller", description = "APIs for managing auction items")
public class AuctionItemController {

    AuctionItemService auctionItemService;

    @PostMapping
    @Operation(summary = "Submit a new item", description = "Seller submits a new item for approval.")
    public ApiResponse<ItemResponse> createItem(
            @RequestBody @Valid ItemRequest request) {
        return ApiResponse.<ItemResponse>builder()
                .result(auctionItemService.createItem(request))
                .build();
    }

    @PatchMapping("/{id}/approve")
    @Operation(summary = "Approve an item", description = "Admin or Auction Manager approves a pending item.")
    public ApiResponse<ItemResponse> approveItem(
            @PathVariable Long id) {
        return ApiResponse.<ItemResponse>builder()
                .result(auctionItemService.approveItem(id))
                .build();
    }

    @PatchMapping("/{id}/reject")
    @Operation(summary = "Reject an item", description = "Admin or Auction Manager rejects a pending item.")
    public ApiResponse<ItemResponse> rejectItem(
            @PathVariable Long id,
            @RequestBody @Valid ItemRejectRequest request) {
        return ApiResponse.<ItemResponse>builder()
                .result(auctionItemService.rejectItem(id, request))
                .build();
    }

    @GetMapping
    @Operation(summary = "Get list of items", description = "Get items with pagination, filtering and role-based access.")
    public ApiResponse<PageResponse<ItemResponse>> getItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) ItemStatus status) {
        return ApiResponse.<PageResponse<ItemResponse>>builder()
                .result(auctionItemService.getItems(page, size, name, categoryId, status))
                .build();
    }
}
