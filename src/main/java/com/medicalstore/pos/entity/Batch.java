package com.medicalstore.pos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "batches", indexes = {
    @Index(name = "idx_batch_medicine", columnList = "medicine_id"),
    @Index(name = "idx_batch_expiry", columnList = "expiryDate"),
    @Index(name = "idx_batch_number", columnList = "batchNumber"),
    @Index(name = "idx_batch_medicine_expiry", columnList = "medicine_id,expiryDate")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Batch {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false, foreignKey = @ForeignKey(name = "fk_batch_medicine"))
    private Medicine medicine;
    
    @Column(nullable = false, length = 50)
    private String batchNumber;
    
    @Column(nullable = false)
    private LocalDate expiryDate;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal purchasePrice;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal sellingPrice;
    
    @Column(nullable = false)
    private Integer quantityAvailable;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @Version
    private Long version; // Optimistic locking for concurrent updates
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public boolean isExpired() {
        return expiryDate.isBefore(LocalDate.now());
    }
    
    public boolean hasStock(int quantity) {
        return quantityAvailable >= quantity;
    }
}



