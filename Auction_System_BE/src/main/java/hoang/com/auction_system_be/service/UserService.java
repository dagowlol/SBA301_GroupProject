package hoang.com.auction_system_be.service;

import hoang.com.auction_system_be.dto.request.RoleAssignRequest;
import hoang.com.auction_system_be.dto.request.UserCreateRequest;
import hoang.com.auction_system_be.dto.request.UserStatusUpdateRequest;
import hoang.com.auction_system_be.dto.request.UserUpdateRequest;
import hoang.com.auction_system_be.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse createUser(UserCreateRequest request);
    UserResponse updateUser(Long id, UserUpdateRequest request);
    UserResponse getUserById(Long id);
    List<UserResponse> getAllUsers();
    UserResponse assignRole(Long id, RoleAssignRequest request);
    UserResponse updateStatus(Long id, UserStatusUpdateRequest request);
    void deleteUser(Long id);
}
