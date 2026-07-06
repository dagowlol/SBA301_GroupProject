package hoang.com.auction_system_be.service.user;

import hoang.com.auction_system_be.dto.request.ChangePasswordRequest;
import hoang.com.auction_system_be.dto.request.RoleAssignRequest;
import hoang.com.auction_system_be.dto.request.UserCreateRequest;
import hoang.com.auction_system_be.dto.request.UserStatusUpdateRequest;
import hoang.com.auction_system_be.dto.request.UserUpdateRequest;
import hoang.com.auction_system_be.dto.response.UserResponse;
import hoang.com.auction_system_be.entity.User;
import hoang.com.auction_system_be.exception.AppException;
import hoang.com.auction_system_be.exception.ErrorCode;
import hoang.com.auction_system_be.mapper.UserMapper;
import hoang.com.auction_system_be.repository.UserRepository;
import hoang.com.auction_system_be.service.auth.SecurityContextService;
import hoang.com.auction_system_be.service.common.mail.MailService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    UserMapper userMapper;
    SecurityContextService securityContextService;
    MailService mailService;

    @Override
    public UserResponse createUser(UserCreateRequest request) {
        securityContextService.checkAdminUser();
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                // role and status will use defaults (USER and ACTIVE) defined in entity
                .build();

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (request.getFirstName() != null)
            user.setFirstName(request.getFirstName());
        if (request.getLastName() != null)
            user.setLastName(request.getLastName());
        if (request.getPhoneNumber() != null)
            user.setPhoneNumber(request.getPhoneNumber());
        if (request.getAddress() != null)
            user.setAddress(request.getAddress());

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toUserResponse(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        securityContextService.checkAdminUser();
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse assignRole(Long id, RoleAssignRequest request) {
        securityContextService.checkAdminUser();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setRole(request.getRole());
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    public UserResponse updateStatus(Long id, UserStatusUpdateRequest request) {
        securityContextService.checkAdminUser();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setStatus(request.getStatus());
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    public void deleteUser(Long id) {
        securityContextService.checkAdminUser();
        if (!userRepository.existsById(id)) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        userRepository.deleteById(id);
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        User user = securityContextService.getCurrentUserEntity();

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        mailService.sendPasswordChangedEmail(user.getEmail());
    }
}
