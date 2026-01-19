package com.medicalstore.pos.dto.response;

import com.medicalstore.pos.entity.Medicine;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicineResponse {
    private Long id;
    private String name;
    private String manufacturer;
    private String category;
    private String barcode; // GTIN/EAN - identifies product
    private String hsnCode;
    private BigDecimal gstPercentage;
    private Boolean prescriptionRequired;
    private Medicine.Status status;
    
    // Stock information (real-time)
    private Integer totalStock;           // Total stock across all batches
    private Integer availableStock;       // Stock in non-expired batches only
    private Boolean lowStock;              // True if stock is below threshold
    private Boolean outOfStock;           // True if no available stock
    private Integer lowStockThreshold;    // Threshold for low stock alert (default: 10)
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

