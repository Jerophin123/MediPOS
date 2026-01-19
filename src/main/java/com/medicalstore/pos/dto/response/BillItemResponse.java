package com.medicalstore.pos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillItemResponse {
    private Long id;
    private Long medicineId;
    private String medicineName;
    private String batchNumber;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal gstPercentage;
    private BigDecimal gstAmount;
    private BigDecimal totalAmount;
}



