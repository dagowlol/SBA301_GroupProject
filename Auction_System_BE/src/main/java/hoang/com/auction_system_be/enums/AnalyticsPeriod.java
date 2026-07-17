package hoang.com.auction_system_be.enums;

import hoang.com.auction_system_be.exception.AppException;
import hoang.com.auction_system_be.exception.ErrorCode;

import java.util.Locale;

public enum AnalyticsPeriod {
    DAILY,
    WEEKLY,
    MONTHLY;

    public static AnalyticsPeriod from(String value) {
        if (value == null || value.isBlank()) {
            throw new AppException(ErrorCode.INVALID_ANALYTICS_PERIOD);
        }

        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new AppException(ErrorCode.INVALID_ANALYTICS_PERIOD);
        }
    }
}
