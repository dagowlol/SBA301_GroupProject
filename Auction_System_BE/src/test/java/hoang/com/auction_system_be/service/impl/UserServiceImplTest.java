package hoang.com.auction_system_be.service.impl;

import hoang.com.auction_system_be.dto.request.RoleAssignRequest;
import hoang.com.auction_system_be.dto.request.UserCreateRequest;
import hoang.com.auction_system_be.dto.request.UserStatusUpdateRequest;
import hoang.com.auction_system_be.dto.request.UserUpdateRequest;
import hoang.com.auction_system_be.dto.response.UserResponse;
import hoang.com.auction_system_be.entity.User;
import hoang.com.auction_system_be.enums.RoleName;
import hoang.com.auction_system_be.enums.UserStatus;
import hoang.com.auction_system_be.exception.AppException;
import hoang.com.auction_system_be.exception.ErrorCode;
import hoang.com.auction_system_be.mapper.UserMapper;
import hoang.com.auction_system_be.repository.UserRepository;
import hoang.com.auction_system_be.service.auth.SecurityContextService;
import hoang.com.auction_system_be.service.user.UserServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private SecurityContextService securityContextService;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("1234567890")
                .passwordHash("encodedPassword")
                .address("123 Main St")
                .status(UserStatus.ACTIVE)
                .role(RoleName.USER)
                .build();

        userResponse = UserResponse.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("1234567890")
                .address("123 Main St")
                .status(UserStatus.ACTIVE)
                .role(RoleName.USER)
                .build();
    }

    @Test
    void createUser_shouldReturnUserResponse_whenValidRequest() {
        UserCreateRequest request = mock(UserCreateRequest.class);
        when(request.getEmail()).thenReturn("john.doe@example.com");
        when(request.getPassword()).thenReturn("password");
        when(request.getFirstName()).thenReturn("John");
        when(request.getLastName()).thenReturn("Doe");
        when(request.getPhoneNumber()).thenReturn("1234567890");
        when(request.getAddress()).thenReturn("123 Main St");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserResponse(any(User.class))).thenReturn(userResponse);

        UserResponse result = userService.createUser(request);

        assertNotNull(result);
        assertEquals(userResponse.getEmail(), result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_shouldThrowException_whenEmailExists() {
        UserCreateRequest request = mock(UserCreateRequest.class);
        when(request.getEmail()).thenReturn("john.doe@example.com");

        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        AppException exception = assertThrows(AppException.class, () -> userService.createUser(request));

        assertEquals(ErrorCode.USER_EXISTED, exception.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_shouldReturnUserResponse_whenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(userResponse.getId(), result.getId());
    }

    @Test
    void getUserById_shouldThrowException_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> userService.getUserById(1L));

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void updateUser_shouldReturnUpdatedUserResponse_whenValidRequest() {
        UserUpdateRequest request = mock(UserUpdateRequest.class);
        when(request.getFirstName()).thenReturn("Jane");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserResponse(any(User.class))).thenReturn(userResponse);

        UserResponse result = userService.updateUser(1L, request);

        assertNotNull(result);
        verify(userRepository).save(user);
    }

    @Test
    void assignRole_shouldReturnUpdatedUserResponse_whenUserExists() {
        RoleAssignRequest request = mock(RoleAssignRequest.class);
        when(request.getRole()).thenReturn(RoleName.ADMIN);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserResponse(any(User.class))).thenReturn(userResponse);

        UserResponse result = userService.assignRole(1L, request);

        assertNotNull(result);
        verify(userRepository).save(user);
    }

    @Test
    void updateStatus_shouldReturnUpdatedUserResponse_whenUserExists() {
        UserStatusUpdateRequest request = mock(UserStatusUpdateRequest.class);
        when(request.getStatus()).thenReturn(UserStatus.SUSPENDED);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserResponse(any(User.class))).thenReturn(userResponse);

        UserResponse result = userService.updateStatus(1L, request);

        assertNotNull(result);
        verify(userRepository).save(user);
    }

    @Test
    void deleteUser_shouldDeleteUser_whenUserExists() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_shouldThrowException_whenUserNotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);

        AppException exception = assertThrows(AppException.class, () -> userService.deleteUser(1L));

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void getAllUsers_shouldReturnListUserResponse() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toUserResponse(any(User.class))).thenReturn(userResponse);

        List<UserResponse> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
