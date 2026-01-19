package com.medicalstore.pos.service;

import com.medicalstore.pos.dto.response.AuditLogResponse;
import com.medicalstore.pos.entity.AuditLog;
import com.medicalstore.pos.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditLogService {
    
    private final AuditLogRepository auditLogRepository;
    
    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }
    
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAllAuditLogs() {
        List<AuditLog> logs = auditLogRepository.findAllByOrderByTimestampDesc();
        return logs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getLoginLogoutLogs() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(30); // Last 30 days
        LocalDateTime endDate = LocalDateTime.now();
        
        List<AuditLog> loginLogs = auditLogRepository.findByActionAndDateRange(
                AuditLog.ActionType.USER_LOGIN, startDate, endDate);
        List<AuditLog> logoutLogs = auditLogRepository.findByActionAndDateRange(
                AuditLog.ActionType.USER_LOGOUT, startDate, endDate);
        
        List<AuditLog> allLogs = loginLogs;
        allLogs.addAll(logoutLogs);
        
        return allLogs.stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<AuditLog> logs = auditLogRepository.findByDateRange(startDate, endDate);
        return logs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogsByUser(Long userId) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();
        List<AuditLog> logs = auditLogRepository.findByUserAndDateRange(userId, startDate, endDate);
        return logs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    private AuditLogResponse mapToResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .userId(log.getUser().getId())
                .username(log.getUser().getUsername())
                .fullName(log.getUser().getFullName())
                .role(log.getUser().getRole().name())
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .description(log.getDescription())
                .oldValue(log.getOldValue())
                .newValue(log.getNewValue())
                .timestamp(log.getTimestamp())
                .ipAddress(log.getIpAddress())
                .build();
    }
}

