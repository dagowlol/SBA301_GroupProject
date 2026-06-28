package hoang.com.auction_system_be.event;

import hoang.com.auction_system_be.dto.response.BidBroadcastResponse;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class BidPlacedEvent extends ApplicationEvent {
    private final String sessionId;
    private final BidBroadcastResponse broadcastResponse;
    private final String userId;
    private final String errorMsg;
    private final boolean isError;

    public BidPlacedEvent(Object source, String sessionId, BidBroadcastResponse broadcastResponse) {
        super(source);
        this.sessionId = sessionId;
        this.broadcastResponse = broadcastResponse;
        this.userId = null;
        this.errorMsg = null;
        this.isError = false;
    }

    public BidPlacedEvent(Object source, String userId, String errorMsg) {
        super(source);
        this.sessionId = null;
        this.broadcastResponse = null;
        this.userId = userId;
        this.errorMsg = errorMsg;
        this.isError = true;
    }
}
