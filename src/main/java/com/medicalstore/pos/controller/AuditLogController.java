package com.medicalstore.pos.controller;

import com.medicalstore.pos.dto.response.AuditLogResponse;
import com.medicalstore.pos.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/audit")
@Tag(name = "Audit Logs", description = "Audit log management APIs (Admin only)")
public class AuditLogController {
    
    private final AuditLogService auditLogService;
    
    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }
    
    @GetMapping("/all")
    @Operation(summary = "Get all audit logs", description = "Retrieve all user activity logs (Admin only)")
    public ResponseEntity<List<AuditLogResponse>> getAllAuditLogs() {
        List<AuditLogResponse> logs = auditLogService.getAllAuditLogs();
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/login-logout")
    @Operation(summary = "Get login/logout history", description = "Retrieve login and logout logs (Admin only)")
    public ResponseEntity<List<AuditLogResponse>> getLoginLogoutLogs() {
        List<AuditLogResponse> logs = auditLogService.getLoginLogoutLogs();
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/date-range")
    @Operation(summary = "Get audit logs by date range", description = "Retrieve audit logs within a date range (Admin only)")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<AuditLogResponse> logs = auditLogService.getAuditLogsByDateRange(startDate, endDate);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get audit logs by user", description = "Retrieve audit logs for a specific user (Admin only)")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogsByUser(@PathVariable Long userId) {
        List<AuditLogResponse> logs = auditLogService.getAuditLogsByUser(userId);
        return ResponseEntity.ok(logs);
    }
}

