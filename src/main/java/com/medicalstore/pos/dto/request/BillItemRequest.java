package com.medicalstore.pos.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BillItemRequest {
    // Either medicineId or barcode must be provided
    private Long medicineId;
    
    private String barcode; // Optional: for barcode scanning
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}

