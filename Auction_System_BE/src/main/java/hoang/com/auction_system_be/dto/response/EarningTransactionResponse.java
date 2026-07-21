package hoang.com.auction_system_be.dto.response;

import hoang.com.auction_system_be.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EarningTransactionResponse(
    String id,
    String productName,
    LocalDateTime sessionEndDate,
    BigDecimal finalPrice,
    String buyerName,
    String paymentStatus,
    Long rawId // Useful for backend to extract nextCursor directly
) {
    // Alternate constructor matching the fields in the JPQL SELECT new expression.
    public EarningTransactionResponse(
        Long sessionId,
        String productName,
        LocalDateTime sessionEndDate,
        BigDecimal finalPrice,
        String firstName,
        String lastName,
        PaymentStatus rawStatus
    ) {
        this(
            "INV-" + sessionId,
            productName,
            sessionEndDate,
            finalPrice,
            maskName(firstName, lastName),
            rawStatus == PaymentStatus.PAID ? "SUCCESS" : "PENDING",
            sessionId
        );
    }

    private static String maskName(String firstName, String lastName) {
        if ((firstName == null || firstName.isBlank()) && (lastName == null || lastName.isBlank())) {
            return "Anonymous";
        }
        String fullName = (lastName != null && !lastName.isBlank() ? lastName.trim() + " " : "") +
                          (firstName != null ? firstName.trim() : "");
        fullName = fullName.trim();
        String[] parts = fullName.split("\\s+");
        if (parts.length <= 1) return parts[0].charAt(0) + "***";
        return parts[0] + " " + parts[1].charAt(0) + "***";
    }
}
