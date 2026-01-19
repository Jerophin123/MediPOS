package com.medicalstore.pos.service;

import com.medicalstore.pos.entity.AuditLog;
import com.medicalstore.pos.entity.User;
import com.medicalstore.pos.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuditService {
    
    private final AuditLogRepository auditLogRepository;
    
    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(AuditLog.ActionType action, User user, String entityType, String entityId,
                    String description, String oldValue, String newValue, HttpServletRequest request) {
        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .user(user)
                .entityType(entityType)
                .entityId(entityId)
                .description(description)
                .oldValue(oldValue)
                .newValue(newValue)
                .timestamp(LocalDateTime.now())
                .ipAddress(request != null ? getClientIpAddress(request) : null)
                .build();
        
        auditLogRepository.save(auditLog);
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}



