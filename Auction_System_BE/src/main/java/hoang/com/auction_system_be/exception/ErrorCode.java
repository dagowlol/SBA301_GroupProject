package hoang.com.auction_system_be.exception;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
@Getter
public enum ErrorCode {
    // ─── General ──────────────────────────────────────────────────────────
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    // ─── Authentication & Authorization ───────────────────────────────────
    UNAUTHENTICATED(1001, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1002, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_CREDENTIALS(1003, "Invalid email or password", HttpStatus.UNAUTHORIZED),
    INVALID_OTP(1004, "Invalid or expired OTP", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(1005, "Invalid token", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_IS_MISSING(1006, "Refresh token is missing", HttpStatus.UNAUTHORIZED),
    INVALID_REFRESH_TOKEN(1007, "Invalid or expired refresh token", HttpStatus.UNAUTHORIZED),
    INVALID_CSRF_TOKEN(1008, "Invalid CSRF token", HttpStatus.UNAUTHORIZED),
    // ─── User ─────────────────────────────────────────────────────────────
    USER_NOT_FOUND(1009, "User not found", HttpStatus.NOT_FOUND),
    USER_EXISTED(1010, "User existed", HttpStatus.BAD_REQUEST),
    USER_LOCKED(1011, "Account is locked due to too many failed login attempts", HttpStatus.FORBIDDEN),
    USER_NOT_ACTIVE(1012, "Account is not yet activated. Please verify your OTP.", HttpStatus.FORBIDDEN),
    USER_INACTIVE(1013, "Account is inactive. Please contact support.", HttpStatus.FORBIDDEN),
    USER_STATUS_INVALID(1014, "Invalid user status", HttpStatus.INTERNAL_SERVER_ERROR),
    ALREADY_AUTHENTICATED(1016, "You are already logged in", HttpStatus.BAD_REQUEST),
    // ─── Role ─────────────────────────────────────────────────────────────
    ROLE_NOT_FOUND(1015, "Role not found", HttpStatus.NOT_FOUND),
    // ─── Category ─────────────────────────────────────────────────────────
    CATEGORY_NOT_FOUND(2001, "Category not found", HttpStatus.NOT_FOUND),
    CATEGORY_NAME_EXISTED(2002, "Category name already existed", HttpStatus.BAD_REQUEST),
    // ─── Item ─────────────────────────────────────────────────────────────
    ITEM_NOT_FOUND(4001, "Item not found", HttpStatus.NOT_FOUND),
    INVALID_ITEM_STATUS(4002, "Invalid item status for this operation", HttpStatus.BAD_REQUEST),

    // Auction Session
    SESSION_NOT_FOUND(6001, "Auction session not found", HttpStatus.NOT_FOUND),
    SESSION_NOT_ACTIVE(6002, "Auction session is not active", HttpStatus.BAD_REQUEST),
    INVALID_BID_AMOUNT(6003, "Bid amount is too low", HttpStatus.BAD_REQUEST),

    // Payment
    PAYMENT_NOT_FOUND(5001, "Payment not found", HttpStatus.NOT_FOUND),
    INVALID_PAYMENT_STATUS(5002, "Invalid payment status for this operation", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
    public int getHttpStatus() {
        return ((HttpStatus) statusCode).value();
    }
}
