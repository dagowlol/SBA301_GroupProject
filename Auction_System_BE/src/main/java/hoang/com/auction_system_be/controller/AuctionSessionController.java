package hoang.com.auction_system_be.controller;

import hoang.com.auction_system_be.dto.request.AuctionSessionRequest;
import hoang.com.auction_system_be.dto.request.AuctionSessionUpdateRequest;
import hoang.com.auction_system_be.dto.response.ApiResponse;
import hoang.com.auction_system_be.dto.response.AuctionSessionDetailResponse;
import hoang.com.auction_system_be.dto.response.AuctionSessionListResponse;
import hoang.com.auction_system_be.dto.response.AuctionSessionResponse;
import hoang.com.auction_system_be.dto.response.CursorPageResponse;
import hoang.com.auction_system_be.enums.SessionStatus;
import hoang.com.auction_system_be.service.session.AuctionSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auction-sessions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Auction Session Controller", description = "APIs for auction session operations")
public class AuctionSessionController {
    AuctionSessionService auctionSessionService;

    // ─── Public endpoints ─────────────────────────────────────────────────
    @GetMapping("/{id}/detail")
    @Operation(summary = "Get auction session detail", description = "Returns auction session details including item info, current price, winner, and latest 10 bid logs.")
    public ApiResponse<AuctionSessionDetailResponse> getAuctionSessionDetail(
            @PathVariable Long id) {
        return ApiResponse.<AuctionSessionDetailResponse>builder()
                .result(auctionSessionService.getAuctionSessionDetail(id))
                .build();
    }

    // ─── Staff / Admin CRUD ───────────────────────────────────────────────
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'AUCTION_MANAGER')")
    @Operation(summary = "List auction sessions", description = "Returns a cursor-paginated list of auction sessions with optional search and status filter.")
    public ApiResponse<CursorPageResponse<AuctionSessionListResponse>> getSessions(
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) SessionStatus status) {
        return ApiResponse.<CursorPageResponse<AuctionSessionListResponse>>builder()
                .result(auctionSessionService.getSessions(cursor, size, search, status))
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'AUCTION_MANAGER')")
    @Operation(summary = "Get session by ID", description = "Returns full details of a single auction session by its ID.")
    public ApiResponse<AuctionSessionResponse> getSessionById(@PathVariable Long id) {
        return ApiResponse.<AuctionSessionResponse>builder()
                .result(auctionSessionService.getSessionById(id))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'AUCTION_MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create auction session", description = "Creates a new auction session for an approved item.")
    public ApiResponse<AuctionSessionResponse> createSession(
            @RequestBody AuctionSessionRequest request) {
        return ApiResponse.<AuctionSessionResponse>builder()
                .result(auctionSessionService.createSession(request))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'AUCTION_MANAGER')")
    @Operation(summary = "Update auction session", description = "Updates an existing auction session (end time, reserve price, status, etc.).")
    public ApiResponse<AuctionSessionResponse> updateSession(
            @PathVariable Long id,
            @RequestBody AuctionSessionUpdateRequest request) {
        return ApiResponse.<AuctionSessionResponse>builder()
                .result(auctionSessionService.updateSession(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'AUCTION_MANAGER')")
    @Operation(summary = "Delete auction session", description = "Soft-deletes an auction session. Active sessions cannot be deleted.")
    public ApiResponse<Void> deleteSession(@PathVariable Long id) {
        auctionSessionService.deleteSession(id);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'AUCTION_MANAGER')")
    @Operation(summary = "List deleted auction sessions", description = "Returns a list of soft-deleted auction sessions.")
    public ApiResponse<List<AuctionSessionListResponse>> getDeletedSessions() {
        return ApiResponse.<List<AuctionSessionListResponse>>builder()
                .result(auctionSessionService.getDeletedSessions())
                .build();
    }

    @PatchMapping("/{id}/restore")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'AUCTION_MANAGER')")
    @Operation(summary = "Restore auction session", description = "Restores a soft-deleted auction session.")
    public ApiResponse<Void> restoreSession(@PathVariable Long id) {
        auctionSessionService.restoreSession(id);
        return ApiResponse.<Void>builder().build();
    }
}
