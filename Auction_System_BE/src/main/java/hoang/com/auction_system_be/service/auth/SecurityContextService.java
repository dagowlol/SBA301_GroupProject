package hoang.com.auction_system_be.service.auth;

import hoang.com.auction_system_be.entity.User;

public interface SecurityContextService {
    Long getCurrentUserId();
    User getCurrentUserEntity();
    User checkAdminUser();
    User checkAdminOrManagerUser();
}
