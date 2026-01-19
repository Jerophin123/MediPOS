package com.medicalstore.pos.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateBillRequest {
    @NotEmpty(message = "Bill items cannot be empty")
    @Valid
    private List<BillItemRequest> items;
    
    private String customerName;
    private String customerPhone;
    
    @NotEmpty(message = "At least one payment is required")
    @Valid
    private List<PaymentRequest> payments;
}



