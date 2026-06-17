package hoang.com.auction_system_be.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hoang.com.auction_system_be.dto.response.ApiResponse;
import hoang.com.auction_system_be.service.VNPayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payments/vnpay")
@RequiredArgsConstructor
@Tag(name = "VNPay Management", description = "VNPay specific APIs for creating URLs and handling IPN")
public class VNPayController {

    private final VNPayService vnPayService;

    @Operation(summary = "Create VNPay URL", description = "Generate a secure URL to redirect user to VNPay gateway")
    @GetMapping("/create-url/{paymentId}")
    public ApiResponse<Map<String, String>> createPaymentUrl(
            @Parameter(description = "ID of the payment record")
            @PathVariable Long paymentId, 
            HttpServletRequest request) {
        String url = vnPayService.createVNPayUrl(paymentId, request);
        return ApiResponse.<Map<String, String>>builder()
                .result(Map.of("url", url))
                .build();
    }

    @Operation(summary = "VNPay IPN Webhook", description = "Endpoint for VNPay to send asynchronous payment results")
    @GetMapping("/ipn")
    public ResponseEntity<Map<String, String>> vnpayIpn(@RequestParam Map<String, String> params) {
        Map<String, String> response = vnPayService.processVNPayIpn(params);
        return ResponseEntity.ok(response);
    }
}
