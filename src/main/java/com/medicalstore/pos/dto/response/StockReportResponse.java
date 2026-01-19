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
public class StockReportResponse {
    private LocalDate reportDate;
    private Integer totalMedicines;
    private Integer totalBatches;
    private Integer totalStockQuantity;
    private Integer availableStockQuantity;
    private Integer expiredStockQuantity;
    private Integer lowStockMedicines;
    private Integer outOfStockMedicines;
    private BigDecimal totalStockValue;
    private List<MedicineStockItem> medicineStock;
    private List<ExpiredStockItem> expiredStock;
    private List<LowStockItem> lowStockItems;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MedicineStockItem {
        private Long medicineId;
        private String medicineName;
        private String manufacturer;
        private String category;
        private String hsnCode;
        private Integer totalStock;
        private Integer availableStock;
        private Integer expiredStock;
        private Boolean lowStock;
        private Boolean outOfStock;
        private BigDecimal averageSellingPrice;
        private BigDecimal stockValue;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExpiredStockItem {
        private Long batchId;
        private Long medicineId;
        private String medicineName;
        private String batchNumber;
        private LocalDate expiryDate;
        private Integer quantity;
        private BigDecimal purchasePrice;
        private BigDecimal stockValue;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LowStockItem {
        private Long medicineId;
        private String medicineName;
        private String manufacturer;
        private Integer availableStock;
        private Integer lowStockThreshold;
        private BigDecimal averageSellingPrice;
    }
}



