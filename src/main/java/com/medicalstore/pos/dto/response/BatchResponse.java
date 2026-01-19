package com.medicalstore.pos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchResponse {
    private Long id;
    private Long medicineId;
    private String medicineName;
    private String batchNumber;
    private LocalDate expiryDate;
    private BigDecimal purchasePrice;
    private BigDecimal sellingPrice;
    private Integer quantityAvailable;
    private Boolean expired;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}



