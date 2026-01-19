package com.medicalstore.pos.dto.request;

import com.medicalstore.pos.entity.Payment;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {
    @NotNull(message = "Payment mode is required")
    private Payment.PaymentMode mode;
    
    @NotNull(message = "Amount is required")
    @Min(value = 0, message = "Amount must be positive")
    private BigDecimal amount;
    
    private String paymentReference;
}



