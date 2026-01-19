package com.medicalstore.pos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "returns", indexes = {
    @Index(name = "idx_return_bill", columnList = "original_bill_id"),
    @Index(name = "idx_return_date", columnList = "return_date"),
    @Index(name = "idx_return_number", columnList = "return_number", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Return {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String returnNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_bill_id", nullable = false, foreignKey = @ForeignKey(name = "fk_return_bill"))
    private Bill originalBill;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by_id", nullable = false, foreignKey = @ForeignKey(name = "fk_return_user"))
    private User processedBy;
    
    @Column(nullable = false)
    private LocalDateTime returnDate;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal refundAmount;
    
    @Column(nullable = false, length = 500)
    private String reason;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReturnType returnType;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (returnDate == null) {
            returnDate = LocalDateTime.now();
        }
    }
    
    public enum ReturnType {
        FULL, PARTIAL
    }
}





