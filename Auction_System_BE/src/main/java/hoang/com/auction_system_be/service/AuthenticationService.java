package hoang.com.auction_system_be.service;

import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    public Long getCurrentUserId() {
        // TODO: Extract user ID from SecurityContextHolder when Spring Security is fully configured
        return 1L;
    }
}
