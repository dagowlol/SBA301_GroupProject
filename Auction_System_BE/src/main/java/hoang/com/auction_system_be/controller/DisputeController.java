package hoang.com.auction_system_be.controller;

import hoang.com.auction_system_be.dto.request.DisputeRequest;
import hoang.com.auction_system_be.dto.request.DisputeResolutionRequest;
import hoang.com.auction_system_be.dto.response.ApiResponse;
import hoang.com.auction_system_be.dto.response.DisputeResponse;
import hoang.com.auction_system_be.dto.response.PageResponse;
import hoang.com.auction_system_be.service.dispute.DisputeService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/disputes")
@Tag(name = "Dispute API", description = "API for creating and managing auction disputes")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DisputeController {

    DisputeService disputeService;

    @PostMapping
    @Operation(summary = "Create dispute", description = "Create a new dispute for an auction session. Open to any authenticated user.")
    public ApiResponse<DisputeResponse> createDispute(@Valid @RequestBody DisputeRequest request) {
        return ApiResponse.<DisputeResponse>builder()
                .result(disputeService.createDispute(request))
                .build();
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'AUCTION_MANAGER')")
    @Operation(summary = "Get disputes", description = "Get paginated disputes with optional filters. Requires ADMIN or AUCTION_MANAGER role.")
    public ApiResponse<PageResponse<DisputeResponse>> getDisputes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long sessionId,
            @RequestParam(required = false) Long raisedById) {

        return ApiResponse.<PageResponse<DisputeResponse>>builder()
                .result(disputeService.getDisputes(page, size, sessionId, raisedById))
                .build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'AUCTION_MANAGER')")
    @Operation(summary = "Resolve or update dispute status", description = "Update the status of a dispute. Requires ADMIN or AUCTION_MANAGER role.")
    public ApiResponse<DisputeResponse> updateDisputeStatus(
            @PathVariable Long id,
            @Valid @RequestBody DisputeResolutionRequest request) {

        return ApiResponse.<DisputeResponse>builder()
                .result(disputeService.updateDisputeStatus(id, request))
                .build();
    }
}
