package com.medicalstore.pos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockBarcodeResponse {
    private Long id;
    private Long batchId;
    private String barcode;
    private Boolean sold;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}





