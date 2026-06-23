package hoang.com.auction_system_be.controller;

import hoang.com.auction_system_be.annotation.Idempotent;
import hoang.com.auction_system_be.dto.request.AuctionSessionRequest;
import hoang.com.auction_system_be.dto.request.AuctionSessionUpdateRequest;
import hoang.com.auction_system_be.dto.response.ApiResponse;
import hoang.com.auction_system_be.dto.response.AuctionSessionDetailResponse;
import hoang.com.auction_system_be.dto.response.AuctionSessionResponse;
import hoang.com.auction_system_be.enums.SessionStatus;
import hoang.com.auction_system_be.service.impl.AuctionSessionServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Auction Session Controller", description = "APIs for auction session lifecycle management (Staff/Admin)")
public class AuctionSessionController {

    AuctionSessionServiceImpl auctionSessionService;

    // ─── Public / Authenticated READ endpoints ────────────────────────────

    @GetMapping("/{id}/detail")
    @Operation(summary = "Get real-time session detail",
               description = "Returns session info + current price + winner + latest 10 bids (used by WebSocket room).")
    public ApiResponse<AuctionSessionDetailResponse> getAuctionSessionDetail(@PathVariable Long id) {
        return ApiResponse.<AuctionSessionDetailResponse>builder()
                .result(auctionSessionService.getAuctionSessionDetail(id))
                .build();
    }

    // ─── Staff/Admin CRUD endpoints ───────────────────────────────────────

    
    @PostMapping
    @PreAuthorize("hasAnyAuthority('AUCTION_MANAGER', 'ADMIN')")
    @Idempotent
    @Operation(summary = "Create auction session",
               description = """
                       Creates a new SCHEDULED session for an APPROVED item.
                       - Requires **Idempotency-Key** header (UUID) to prevent double-submit.
                       - Returns **201 Created** with a **Location** header pointing to the new resource.
                       - start_time must be ≥ 5 minutes from now (scheduler buffer).
                       """)
    public ResponseEntity<ApiResponse<AuctionSessionResponse>> createSession(
            @RequestBody @Valid AuctionSessionRequest request,
            UriComponentsBuilder ucb) {

        AuctionSessionResponse created = auctionSessionService.createSession(request);

        // Location: /api/v1/sessions/{id}  (RESTful HATEOAS)
        URI location = ucb.path("/api/v1/sessions/{id}")
                          .buildAndExpand(created.getId())
                          .toUri();

        ApiResponse<AuctionSessionResponse> body = ApiResponse.<AuctionSessionResponse>builder()
                .result(created)
                .message("Auction session created successfully")
                .build();

        return ResponseEntity.created(location).body(body);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('AUCTION_MANAGER', 'ADMIN')")
    @Operation(summary = "Get list of sessions",
               description = "Keyset paginated list. Supports fuzzy search by item name and filter by status.")
    public ApiResponse<hoang.com.auction_system_be.dto.response.CursorPageResponse<hoang.com.auction_system_be.dto.response.AuctionSessionListResponse>> getSessions(
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) SessionStatus status) {
        return ApiResponse.<hoang.com.auction_system_be.dto.response.CursorPageResponse<hoang.com.auction_system_be.dto.response.AuctionSessionListResponse>>builder()
                .result(auctionSessionService.getSessions(cursor, size, search, status))
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('AUCTION_MANAGER', 'ADMIN')")
    @Operation(summary = "Get session by ID",
               description = "Full configuration detail of a specific session for Staff/Admin.")
    public ApiResponse<AuctionSessionResponse> getSessionById(@PathVariable Long id) {
        return ApiResponse.<AuctionSessionResponse>builder()
                .result(auctionSessionService.getSessionById(id))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('AUCTION_MANAGER', 'ADMIN')")
    @Operation(summary = "Update session",
               description = "Update config or transition status. When ACTIVE, only end_time extension is allowed.")
    public ApiResponse<AuctionSessionResponse> updateSession(
            @PathVariable Long id,
            @RequestBody AuctionSessionUpdateRequest request) {
        return ApiResponse.<AuctionSessionResponse>builder()
                .result(auctionSessionService.updateSession(id, request))
                .message("Auction session updated successfully")
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('AUCTION_MANAGER', 'ADMIN')")
    @Operation(summary = "Soft-delete session",
               description = "Sets deleted_at. Cannot delete an ACTIVE session.")
    public ApiResponse<Void> deleteSession(@PathVariable Long id) {
        auctionSessionService.deleteSession(id);
        return ApiResponse.<Void>builder()
                .message("Auction session deleted successfully")
                .build();
    }
}
