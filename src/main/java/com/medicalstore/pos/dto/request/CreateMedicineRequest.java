package com.medicalstore.pos.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CreateMedicineRequest {
    @NotBlank(message = "Medicine name is required")
    private String name;
    
    @NotBlank(message = "Manufacturer is required")
    private String manufacturer;
    
    private String category;
    
    private String barcode; // GTIN/EAN - identifies product
    
    @NotBlank(message = "HSN code is required")
    private String hsnCode;
    
    @NotNull(message = "GST percentage is required")
    @PositiveOrZero(message = "GST percentage must be positive or zero")
    private BigDecimal gstPercentage;
    
    @NotNull(message = "Prescription required flag is required")
    private Boolean prescriptionRequired;
    
    // Optional: Initial stock and pricing (creates batch automatically)
    private Integer initialStock;
    
    private BigDecimal purchasePrice;
    
    private BigDecimal sellingPrice;
    
    private String batchNumber;
    
    @Future(message = "Expiry date must be in the future")
    private LocalDate expiryDate;
    
    // Optional: Individual barcodes for each stock item (must match initialStock if provided)
    private List<String> barcodes;
}

