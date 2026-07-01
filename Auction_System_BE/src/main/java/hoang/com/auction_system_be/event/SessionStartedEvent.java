package hoang.com.auction_system_be.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class SessionStartedEvent extends ApplicationEvent {
    private final Long sessionId;

    public SessionStartedEvent(Object source, Long sessionId) {
        super(source);
        this.sessionId = sessionId;
    }
}
