package com.medicalstore.pos.dto.response;

import com.medicalstore.pos.entity.Payment;
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
public class PaymentResponse {
    private Long id;
    private String paymentReference;
    private Payment.PaymentMode mode;
    private BigDecimal amount;
    private Payment.PaymentStatus status;
    private LocalDateTime paymentDate;
}





