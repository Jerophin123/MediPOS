package com.medicalstore.pos.controller;

import com.medicalstore.pos.dto.request.ChangePasswordRequest;
import com.medicalstore.pos.dto.response.UserResponse;
import com.medicalstore.pos.entity.User;
import com.medicalstore.pos.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "User Management", description = "User management APIs (Admin only)")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve all users (Admin only)")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Retrieve user details by ID (Admin only)")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }
    
    @PutMapping("/{userId}/password")
    @Operation(summary = "Change user password", description = "Change password for a user (Admin only)")
    public ResponseEntity<UserResponse> changeUserPassword(
            @PathVariable Long userId,
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal User adminUser,
            HttpServletRequest httpRequest) {
        UserResponse user = userService.changeUserPassword(userId, request, adminUser, httpRequest);
        return ResponseEntity.ok(user);
    }
    
    @PutMapping("/{userId}/status")
    @Operation(summary = "Update user status", description = "Activate or deactivate a user (Admin only)")
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam Boolean active,
            @AuthenticationPrincipal User adminUser,
            HttpServletRequest httpRequest) {
        UserResponse user = userService.updateUserStatus(userId, active, adminUser, httpRequest);
        return ResponseEntity.ok(user);
    }
}

