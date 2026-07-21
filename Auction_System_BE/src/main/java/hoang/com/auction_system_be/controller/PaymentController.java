package hoang.com.auction_system_be.controller;

import hoang.com.auction_system_be.dto.response.ApiResponse;
import hoang.com.auction_system_be.dto.response.PaymentResponse;
import hoang.com.auction_system_be.service.payment.CorePaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "Generic payment APIs")
public class PaymentController {

    private final CorePaymentService corePaymentService;

    @Operation(summary = "Check payment status", description = "Retrieve payment status using gateway transaction reference")
    @GetMapping("/status")
    public ApiResponse<PaymentResponse> checkStatus(
            @Parameter(description = "Gateway transaction reference (e.g. vnp_TransactionNo)") @RequestParam("vnp_TransactionNo") String vnp_TransactionNo) {
        return ApiResponse.<PaymentResponse>builder()
                .result(corePaymentService.getPaymentStatus(vnp_TransactionNo))
                .build();
    }

    @Operation(summary = "Get my payment for session", description = "Retrieve final payment details for the winner of a session")
    @GetMapping("/session/{sessionId}/my")
    public ApiResponse<PaymentResponse> getMyPayment(@PathVariable Long sessionId) {
        return ApiResponse.<PaymentResponse>builder()
                .result(corePaymentService.getMyPaymentForSession(sessionId))
                .build();
    }
}
