package com.medicalstore.pos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "medicines", indexes = {
    @Index(name = "idx_medicine_name", columnList = "name"),
    @Index(name = "idx_medicine_hsn", columnList = "hsnCode"),
    @Index(name = "idx_medicine_status", columnList = "status"),
    @Index(name = "idx_medicine_barcode", columnList = "barcode")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medicine {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(nullable = false, length = 200)
    private String manufacturer;
    
    @Column(length = 100)
    private String category;
    
    @Column(length = 50)
    private String barcode; // GTIN/EAN - identifies product, NOT unique per unit
    
    @Column(nullable = false, length = 20, unique = true)
    private String hsnCode;
    
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal gstPercentage;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean prescriptionRequired = false;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.ACTIVE;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @Version
    private Long version; // Optimistic locking
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum Status {
        ACTIVE, DISCONTINUED
    }
}

