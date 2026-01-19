package com.medicalstore.pos.dto.response;

import com.medicalstore.pos.entity.Bill;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillResponse {
    private Long id;
    private String billNumber;
    private LocalDateTime billDate;
    private Long cashierId;
    private String cashierName;
    private String customerName;
    private String customerPhone;
    private BigDecimal subtotal;
    private BigDecimal totalGst;
    private BigDecimal totalAmount;
    private Bill.PaymentStatus paymentStatus;
    private Boolean cancelled;
    private String cancellationReason;
    private List<BillItemResponse> items;
    private List<PaymentResponse> payments;
    private LocalDateTime createdAt;
}



