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
    ITEM_NOT_APPROVED(4003, "Item must be in APPROVED status to create an auction session", HttpStatus.UNPROCESSABLE_ENTITY),
    ITEM_ALREADY_SOLD(4004, "Item has already been sold", HttpStatus.CONFLICT),

    // ─── Idempotency ──────────────────────────────────────────────────────
    DUPLICATE_REQUEST(7001, "Duplicate request detected. The same operation was already processed.", HttpStatus.OK),

    // Auction Session
    SESSION_NOT_FOUND(6001, "Auction session not found", HttpStatus.NOT_FOUND),
    SESSION_NOT_ACTIVE(6002, "Auction session is not active", HttpStatus.BAD_REQUEST),
    INVALID_BID_AMOUNT(6003, "Bid amount is too low", HttpStatus.BAD_REQUEST),
    AUCTION_ENDED(6004, "Auction has ended", HttpStatus.BAD_REQUEST),
    SESSION_ALREADY_ACTIVE(6005, "Cannot modify a session that is already ACTIVE", HttpStatus.BAD_REQUEST),
    SESSION_CONFLICT(6006, "Item already has an active or scheduled session", HttpStatus.CONFLICT),
    SESSION_INVALID_END_TIME(6007, "End time must be after start time", HttpStatus.BAD_REQUEST),
    SESSION_CANNOT_ROLLBACK_END_TIME(6008, "Cannot set end time to a past value for an active session", HttpStatus.BAD_REQUEST),
    SESSION_ITEM_LOCKED(6009, "Cannot change item of an active session", HttpStatus.BAD_REQUEST),
    SEARCH_KEYWORD_TOO_SHORT(6010, "Search keyword must be at least 2 characters long", HttpStatus.BAD_REQUEST),
    TOO_MANY_REQUESTS(6005, "Too many requests, please slow down", HttpStatus.TOO_MANY_REQUESTS),

    // Payment
    PAYMENT_NOT_FOUND(5001, "Payment not found", HttpStatus.NOT_FOUND),
    INVALID_PAYMENT_STATUS(5002, "Invalid payment status for this operation", HttpStatus.BAD_REQUEST),
    // System
    SYSTEM_BUSY(8001, "System is busy. Please try again later.", HttpStatus.SERVICE_UNAVAILABLE),
    SYSTEM_ERROR(8002, "System error occurred.", HttpStatus.INTERNAL_SERVER_ERROR),

    // Dispute
    DISPUTE_NOT_FOUND(7001, "Dispute not found", HttpStatus.NOT_FOUND);

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
