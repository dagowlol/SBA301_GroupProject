package hoang.com.auction_system_be.service.auth;

import hoang.com.auction_system_be.enums.RoleName;
import hoang.com.auction_system_be.security.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import hoang.com.auction_system_be.entity.User;
import hoang.com.auction_system_be.repository.UserRepository;
import hoang.com.auction_system_be.exception.AppException;
import hoang.com.auction_system_be.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class SecurityContextServiceImpl implements SecurityContextService {

    private final UserRepository userRepository;

    @Override
    public Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl userDetails) {
            return userDetails.getId();
        }
        return null;
    }

    @Override
    public User getCurrentUserEntity() {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    public User checkAdminUser() {
        User user = getCurrentUserEntity();
        if (user.getRole() != RoleName.ADMIN) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return user;
    }

    @Override
    public User checkAdminOrManagerUser() {
        User user = getCurrentUserEntity();
        if (user.getRole() != RoleName.ADMIN && user.getRole() != RoleName.AUCTION_MANAGER) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return user;
    }
}
