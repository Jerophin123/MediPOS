package com.medicalstore.pos.dto.response;

import com.medicalstore.pos.entity.AuditLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponse {
    private Long id;
    private Long userId;
    private String username;
    private String fullName;
    private String role;
    private AuditLog.ActionType action;
    private String entityType;
    private String entityId;
    private String description;
    private String oldValue;
    private String newValue;
    private LocalDateTime timestamp;
    private String ipAddress;
}



