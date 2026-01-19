package com.medicalstore.pos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GstReportResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalCgst;
    private BigDecimal totalSgst;
    private BigDecimal totalGst;
    private List<GstBreakup> gstBreakup;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GstBreakup {
        private String hsnCode;
        private String medicineName;
        private BigDecimal gstPercentage;
        private BigDecimal taxableAmount;
        private BigDecimal cgst;
        private BigDecimal sgst;
        private BigDecimal totalGst;
    }
}



