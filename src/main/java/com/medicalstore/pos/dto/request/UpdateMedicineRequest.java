package com.medicalstore.pos.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateMedicineRequest {
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
}

