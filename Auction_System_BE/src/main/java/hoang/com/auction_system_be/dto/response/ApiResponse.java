package hoang.com.auction_system_be.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import hoang.com.auction_system_be.exception.ErrorCode;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    @Builder.Default
    int code = 1000;
    @Builder.Default
    String message = "success";
    T result;

    // ─── Success shortcuts ────────────────────────────────────────────────
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .code(1000)
                .result(data)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, "success");
    }

    public static ApiResponse<Void> success(String message) {
        return ApiResponse.<Void>builder()
                .code(1000)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> success() {
        return success(null, "success");
    }

    // ─── Failure shortcuts ────────────────────────────────────────────────
    public static <T> ApiResponse<T> failure(int code, String errorCode, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> failure(ErrorCode errorCode) {
        return failure(
                errorCode.getCode(),
                String.valueOf(errorCode.getCode()),
                errorCode.getMessage());
    }
}
