package com.medicalstore.pos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_user", columnList = "user_id"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_date", columnList = "timestamp")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_audit_user"))
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ActionType action;
    
    @Column(nullable = false, length = 100)
    private String entityType;
    
    @Column(length = 100)
    private String entityId;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(columnDefinition = "TEXT")
    private String oldValue;
    
    @Column(columnDefinition = "TEXT")
    private String newValue;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(length = 50)
    private String ipAddress;
    
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
    
    public enum ActionType {
        BILL_CREATED,
        BILL_CANCELLED,
        PAYMENT_RECEIVED,
        REFUND_PROCESSED,
        STOCK_ADJUSTED,
        STOCK_UPDATED,
        PRICE_OVERRIDE,
        MEDICINE_ADDED,
        MEDICINE_UPDATED,
        MEDICINE_DELETED,
        BATCH_ADDED,
        BATCH_UPDATED,
        BATCH_DELETED,
        USER_LOGIN,
        USER_LOGOUT,
        USER_PASSWORD_CHANGED,
        USER_STATUS_CHANGED
    }
}

