package com.medicalstore.pos.service;

import com.medicalstore.pos.dto.request.LoginRequest;
import com.medicalstore.pos.dto.response.AuthResponse;
import com.medicalstore.pos.entity.AuditLog;
import com.medicalstore.pos.entity.User;
import com.medicalstore.pos.repository.UserRepository;
import com.medicalstore.pos.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final AuditService auditService;
    
    public AuthService(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider,
                      UserRepository userRepository, AuditService auditService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }
    
    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);
        
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        auditService.log(AuditLog.ActionType.USER_LOGIN, user, "User", 
                        user.getId().toString(), "User logged in",
                        null, null, httpRequest);
        
        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
    
    public void logout(HttpServletRequest httpRequest) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByUsername(username)
                    .orElse(null);
            
            if (user != null) {
                auditService.log(AuditLog.ActionType.USER_LOGOUT, user, "User", 
                        user.getId().toString(), "User logged out",
                        null, null, httpRequest);
            }
        } catch (Exception e) {
            // Log error but don't throw - logout should always succeed
            // This handles cases where user might already be logged out
        }
    }
}

