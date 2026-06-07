package hoang.com.auction_system_be.dto.response;
import com.fasterxml.jackson.annotation.JsonInclude;
import hoang.com.auction_system_be.enums.ResponseStatus;
import hoang.com.auction_system_be.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private ResponseStatus status;
    private String errorCode;
    private T data;
    private String message;
    private Integer httpStatus;
    // ─── Success shortcuts ────────────────────────────────────────────────
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .status(ResponseStatus.SUCCESS)
                .data(data)
                .message(message)
                .httpStatus(HttpStatus.OK.value())
                .build();
    }
    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Operation successful.");
    }
    public static ApiResponse<Void> success(String message) {
        return ApiResponse.<Void>builder()
                .status(ResponseStatus.SUCCESS)
                .message(message)
                .httpStatus(HttpStatus.OK.value())
                .build();
    }
    public static <T> ApiResponse<T> success() {
        return success(null, "Operation successful.");
    }
    // ─── Failure shortcuts ────────────────────────────────────────────────
    public static <T> ApiResponse<T> failure(int httpStatus, String errorCode, String message) {
        return ApiResponse.<T>builder()
                .status(ResponseStatus.FAILURE)
                .errorCode(errorCode)
                .message(message)
                .httpStatus(httpStatus)
                .build();
    }
    public static <T> ApiResponse<T> failure(ErrorCode errorCode) {
        return failure(
                errorCode.getHttpStatus(),
                String.valueOf(errorCode.getCode()),
                errorCode.getMessage());
    }
}
