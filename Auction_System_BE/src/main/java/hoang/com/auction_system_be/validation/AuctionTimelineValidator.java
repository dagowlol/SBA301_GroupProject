package hoang.com.auction_system_be.validation;

import hoang.com.auction_system_be.dto.request.AuctionSessionRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class AuctionTimelineValidator
        implements ConstraintValidator<ValidAuctionTimeline, AuctionSessionRequest> {

    @Override
    public boolean isValid(AuctionSessionRequest request, ConstraintValidatorContext ctx) {
        if (request == null
                || request.getStartTime() == null
                || request.getEndTime() == null) {
            return true;
        }
        return request.getEndTime().isAfter(request.getStartTime());
    }
}
