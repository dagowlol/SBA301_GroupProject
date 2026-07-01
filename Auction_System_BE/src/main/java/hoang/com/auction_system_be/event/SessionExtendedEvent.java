package hoang.com.auction_system_be.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

@Getter
public class SessionExtendedEvent extends ApplicationEvent {
    private final String sessionId;
    private final Map<String, Object> payload;

    public SessionExtendedEvent(Object source, String sessionId, Map<String, Object> payload) {
        super(source);
        this.sessionId = sessionId;
        this.payload = payload;
    }
}
