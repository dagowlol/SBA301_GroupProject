package hoang.com.auction_system_be.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

@Getter
public class AutoBidLimitReachedEvent extends ApplicationEvent {
    private final Long userId;
    private final Long sessionId;
    private final BigDecimal maxBidAmount;

    public AutoBidLimitReachedEvent(Object source, Long userId, Long sessionId, BigDecimal maxBidAmount) {
        super(source);
        this.userId = userId;
        this.sessionId = sessionId;
        this.maxBidAmount = maxBidAmount;
    }
}
