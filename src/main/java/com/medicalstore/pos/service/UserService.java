package com.medicalstore.pos.service;

import com.medicalstore.pos.dto.request.ChangePasswordRequest;
import com.medicalstore.pos.dto.response.UserResponse;
import com.medicalstore.pos.entity.User;
import com.medicalstore.pos.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }
    
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return mapToResponse(user);
    }
    
    @Transactional
    public UserResponse changeUserPassword(Long userId, ChangePasswordRequest request, User adminUser, jakarta.servlet.http.HttpServletRequest httpRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        String oldPasswordHash = user.getPassword();
        String newPasswordHash = passwordEncoder.encode(request.getNewPassword());
        
        user.setPassword(newPasswordHash);
        userRepository.save(user);
        
        // Log the password change
        auditService.log(
            com.medicalstore.pos.entity.AuditLog.ActionType.USER_PASSWORD_CHANGED,
            adminUser,
            "User",
            user.getId().toString(),
            "Admin changed password for user: " + user.getUsername(),
            null,
            "Password changed",
            httpRequest
        );
        
        return mapToResponse(user);
    }
    
    @Transactional
    public UserResponse updateUserStatus(Long userId, Boolean active, User adminUser, jakarta.servlet.http.HttpServletRequest httpRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        Boolean oldStatus = user.getActive();
        user.setActive(active);
        userRepository.save(user);
        
        // Log the status change
        auditService.log(
            com.medicalstore.pos.entity.AuditLog.ActionType.USER_STATUS_CHANGED,
            adminUser,
            "User",
            user.getId().toString(),
            "Admin " + (active ? "activated" : "deactivated") + " user: " + user.getUsername(),
            oldStatus.toString(),
            active.toString(),
            httpRequest
        );
        
        return mapToResponse(user);
    }
    
    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

