package hoang.com.auction_system_be.event;

import hoang.com.auction_system_be.entity.AuctionSession;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Fired after an AuctionSession is successfully committed to DB.
 * Consumers (e.g. notification, background job) listen via
 * {@code @TransactionalEventListener(phase = AFTER_COMMIT)} so they
 * only execute once the DB record is guaranteed to exist.
 */
@Getter
public class AuctionSessionCreatedEvent extends ApplicationEvent {

    private final AuctionSession session;
    private final Long createdByUserId;

    public AuctionSessionCreatedEvent(Object source, AuctionSession session, Long createdByUserId) {
        super(source);
        this.session = session;
        this.createdByUserId = createdByUserId;
    }
}
