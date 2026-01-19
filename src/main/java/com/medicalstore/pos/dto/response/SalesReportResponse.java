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
public class SalesReportResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalBills;
    private BigDecimal totalSales;
    private BigDecimal totalGst;
    private BigDecimal totalCash;
    private BigDecimal totalUpi;
    private BigDecimal totalCard;
    private List<DailySales> dailySales;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailySales {
        private LocalDate date;
        private Integer billCount;
        private BigDecimal totalAmount;
    }
}





