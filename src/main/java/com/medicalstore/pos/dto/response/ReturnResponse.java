package com.medicalstore.pos.dto.response;

import com.medicalstore.pos.entity.Return;
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
public class ReturnResponse {
    private Long id;
    private String returnNumber;
    private Long billId;
    private String billNumber;
    private Long processedById;
    private String processedByName;
    private LocalDateTime returnDate;
    private BigDecimal refundAmount;
    private String reason;
    private Return.ReturnType returnType;
    private LocalDateTime createdAt;
    private List<ReturnItemResponse> items;
}

