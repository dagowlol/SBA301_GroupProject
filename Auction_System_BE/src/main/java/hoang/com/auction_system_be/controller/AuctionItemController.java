package hoang.com.auction_system_be.controller;

import hoang.com.auction_system_be.dto.request.ItemRejectRequest;
import hoang.com.auction_system_be.dto.request.ItemRequest;
import hoang.com.auction_system_be.dto.request.UpdateItemRequest;
import hoang.com.auction_system_be.dto.response.ApiResponse;
import hoang.com.auction_system_be.dto.response.ItemResponse;
import hoang.com.auction_system_be.dto.response.PageResponse;
import hoang.com.auction_system_be.enums.ItemStatus;
import hoang.com.auction_system_be.service.item.AuctionItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Auction Item Controller", description = "APIs for managing auction items")
public class AuctionItemController {

        AuctionItemService auctionItemService;

        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "Submit a new item", description = "Seller submits a new item for approval.")
        public ApiResponse<ItemResponse> createItem(
                        @ModelAttribute @Valid ItemRequest request) {
                return ApiResponse.<ItemResponse>builder()
                                .result(auctionItemService.createItem(request))
                                .build();
        }

        @PatchMapping(value = "/{id}/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize("hasAnyAuthority('ADMIN', 'AUCTION_MANAGER')")
        @Operation(summary = "Update an item", description = "Admin or Auction Manager updates a pending item.")
        public ApiResponse<ItemResponse> updateItem(
                        @PathVariable Long id, @ModelAttribute UpdateItemRequest request) {
                return ApiResponse.<ItemResponse>builder()
                                .result(auctionItemService.updateItem(id, request))
                                .build();
        }

        @PatchMapping(value = "/my-uploaded/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "Update own item", description = "Seller updates and resubmits a pending or rejected item.")
        public ApiResponse<ItemResponse> updateMyItem(
                        @PathVariable Long id, @ModelAttribute UpdateItemRequest request) {
                return ApiResponse.<ItemResponse>builder()
                                .result(auctionItemService.updateMyItem(id, request))
                                .build();
        }

        @PatchMapping("/{id}/approve")
        @PreAuthorize("hasAnyAuthority('ADMIN', 'AUCTION_MANAGER')")
        @Operation(summary = "Approve an item", description = "Admin or Auction Manager approves a pending item.")
        public ApiResponse<ItemResponse> approveItem(
                        @PathVariable Long id) {
                return ApiResponse.<ItemResponse>builder()
                                .result(auctionItemService.approveItem(id))
                                .build();
        }

        @PatchMapping("/{id}/reject")
        @PreAuthorize("hasAnyAuthority('ADMIN', 'AUCTION_MANAGER')")
        @Operation(summary = "Reject an item", description = "Admin or Auction Manager rejects a pending item.")
        public ApiResponse<ItemResponse> rejectItem(
                        @PathVariable Long id,
                        @RequestBody @Valid ItemRejectRequest request) {
                return ApiResponse.<ItemResponse>builder()
                                .result(auctionItemService.rejectItem(id, request))
                                .build();
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("hasAnyAuthority('ADMIN', 'AUCTION_MANAGER')")
        @Operation(summary = "Delete an item", description = "Admin or Auction Manager soft-deletes an auction item.")
        public ApiResponse<Void> deleteItem(@PathVariable Long id) {
                auctionItemService.deleteItem(id);
                return ApiResponse.<Void>builder()
                                .message("Item deleted successfully")
                                .build();
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get item by ID", description = "Returns full details of a single auction item by its ID.")
        public ApiResponse<ItemResponse> getItemById(@PathVariable Long id) {
                return ApiResponse.<ItemResponse>builder()
                                .result(auctionItemService.getItemById(id))
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

        @GetMapping("/my-uploaded")
        @Operation(summary = "Get user's uploaded items", description = "Get items uploaded by the authenticated user with pagination and filters.")
        public ApiResponse<PageResponse<ItemResponse>> getMyUploadedItems(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(required = false) String name,
                        @RequestParam(required = false) Long categoryId,
                        @RequestParam(required = false) ItemStatus status) {
                return ApiResponse.<PageResponse<ItemResponse>>builder()
                                .result(auctionItemService.getMyUploadedItems(page, size, name, categoryId, status))
                                .build();
        }

        @GetMapping("/my-won")
        @Operation(summary = "Get user's won items", description = "Get items won by the authenticated user with pagination and filters.")
        public ApiResponse<PageResponse<ItemResponse>> getMyWonItems(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(required = false) String name,
                        @RequestParam(required = false) Long categoryId) {
                return ApiResponse.<PageResponse<ItemResponse>>builder()
                                .result(auctionItemService.getMyWonItems(page, size, name, categoryId))
                                .build();
        }
}

