package hoang.com.auction_system_be.controller;

import hoang.com.auction_system_be.dto.request.ChangePasswordRequest;
import hoang.com.auction_system_be.dto.request.RoleAssignRequest;
import hoang.com.auction_system_be.dto.request.UserCreateRequest;
import hoang.com.auction_system_be.dto.request.UserStatusUpdateRequest;
import hoang.com.auction_system_be.dto.request.UserUpdateRequest;
import hoang.com.auction_system_be.dto.response.ApiResponse;
import hoang.com.auction_system_be.dto.response.UserResponse;
import hoang.com.auction_system_be.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "User Management", description = "APIs for managing users, roles and statuses")
public class UserController {

    UserService userService;

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Create a new user")
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.createUser(request))
                .build();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Get all users")
    public ApiResponse<List<UserResponse>> getAllUsers() {
        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getAllUsers())
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ApiResponse<UserResponse> getUserById(@PathVariable Long id) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUserById(id))
                .build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user information")
    public ApiResponse<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUser(id, request))
                .build();
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Assign role to user (Admin only)")
    public ApiResponse<UserResponse> assignRole(@PathVariable Long id, @RequestBody RoleAssignRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.assignRole(id, request))
                .build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Update user status (Suspend/Activate)")
    public ApiResponse<UserResponse> updateStatus(@PathVariable Long id, @RequestBody UserStatusUpdateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateStatus(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Delete user by ID (Admin only)")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.<Void>builder()
                .message("User deleted successfully")
                .build();
    }

    @PutMapping("/password")
    @Operation(summary = "Change password for the authenticated user")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ApiResponse.<Void>builder()
                .message("Password changed successfully. A notification email has been sent.")
                .build();
    }
}
