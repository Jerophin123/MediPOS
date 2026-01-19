package com.medicalstore.pos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_barcodes", indexes = {
    @Index(name = "idx_barcode_unique", columnList = "barcode", unique = true),
    @Index(name = "idx_barcode_batch", columnList = "batch_id"),
    @Index(name = "idx_barcode_sold", columnList = "sold")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockBarcode {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false, foreignKey = @ForeignKey(name = "fk_barcode_batch"))
    private Batch batch;
    
    @Column(nullable = false, length = 100, unique = true)
    private String barcode; // Unique barcode for each individual item
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean sold = false; // Track if this item has been sold
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}



