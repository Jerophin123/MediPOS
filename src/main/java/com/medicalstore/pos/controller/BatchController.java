package com.medicalstore.pos.controller;

import com.medicalstore.pos.dto.request.AddBarcodesRequest;
import com.medicalstore.pos.dto.request.CreateBatchRequest;
import com.medicalstore.pos.dto.request.UpdateBatchRequest;
import com.medicalstore.pos.dto.request.UpdateStockRequest;
import com.medicalstore.pos.dto.response.BatchResponse;
import com.medicalstore.pos.dto.response.StockBarcodeResponse;
import com.medicalstore.pos.entity.User;
import com.medicalstore.pos.service.BatchService;
import com.medicalstore.pos.service.StockBarcodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pharmacist/batches")
@Tag(name = "Batch Management", description = "Batch and inventory management APIs")
@SecurityRequirement(name = "bearerAuth")
public class BatchController {
    
    private final BatchService batchService;
    private final StockBarcodeService stockBarcodeService;
    
    public BatchController(BatchService batchService, StockBarcodeService stockBarcodeService) {
        this.batchService = batchService;
        this.stockBarcodeService = stockBarcodeService;
    }
    
    @PostMapping
    @Operation(summary = "Create batch", description = "Add a new batch for a medicine")
    public ResponseEntity<BatchResponse> createBatch(
            @Valid @RequestBody CreateBatchRequest request,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpRequest) {
        BatchResponse response = batchService.createBatch(request, user, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/medicine/{medicineId}")
    @Operation(summary = "Get batches by medicine", description = "Retrieve all batches for a medicine")
    public ResponseEntity<List<BatchResponse>> getBatchesByMedicine(@PathVariable Long medicineId) {
        List<BatchResponse> response = batchService.getBatchesByMedicine(medicineId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/expired")
    @Operation(summary = "Get expired batches", description = "Retrieve all expired batches")
    public ResponseEntity<List<BatchResponse>> getExpiredBatches() {
        List<BatchResponse> response = batchService.getExpiredBatches();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/low-stock")
    @Operation(summary = "Get low stock batches", description = "Retrieve batches with low stock")
    public ResponseEntity<List<BatchResponse>> getLowStockBatches(
            @RequestParam(defaultValue = "10") Integer threshold) {
        List<BatchResponse> response = batchService.getLowStockBatches(threshold);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(summary = "Get all batches", description = "Retrieve all batches ordered by creation date (purchase history)")
    public ResponseEntity<List<BatchResponse>> getAllBatches() {
        List<BatchResponse> response = batchService.getAllBatches();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/barcode/{barcode}")
    @Operation(summary = "Get batch by barcode", description = "Retrieve batch details for a specific barcode")
    public ResponseEntity<BatchResponse> getBatchByBarcode(@PathVariable String barcode) {
        com.medicalstore.pos.entity.Batch batch = stockBarcodeService.getBatchByBarcode(barcode);
        BatchResponse response = batchService.getBatchById(batch.getId());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get batch by ID", description = "Retrieve batch details by ID")
    public ResponseEntity<BatchResponse> getBatchById(@PathVariable Long id) {
        BatchResponse response = batchService.getBatchById(id);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update batch", description = "Update batch details")
    public ResponseEntity<BatchResponse> updateBatch(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBatchRequest request,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpRequest) {
        BatchResponse response = batchService.updateBatch(id, request, user, httpRequest);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/stock")
    @Operation(summary = "Update stock quantity", description = "Update the available quantity for a batch")
    public ResponseEntity<BatchResponse> updateStock(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStockRequest request,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpRequest) {
        BatchResponse response = batchService.updateStock(id, request, user, httpRequest);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete batch", description = "Delete a batch (only if not used in bills)")
    public ResponseEntity<Void> deleteBatch(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpRequest) {
        batchService.deleteBatch(id, user, httpRequest);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{id}/barcodes")
    @Operation(summary = "Get barcodes for batch", description = "Retrieve all barcodes (available and sold) for a batch")
    public ResponseEntity<List<StockBarcodeResponse>> getBarcodesByBatch(@PathVariable Long id) {
        List<StockBarcodeResponse> response = stockBarcodeService.getBarcodesByBatchId(id);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/barcodes")
    @Operation(summary = "Add barcodes to batch", description = "Add new barcodes to a batch and update stock count")
    public ResponseEntity<List<StockBarcodeResponse>> addBarcodesToBatch(
            @PathVariable Long id,
            @Valid @RequestBody AddBarcodesRequest request,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpRequest) {
        List<StockBarcodeResponse> response = stockBarcodeService.addBarcodesToBatch(id, request, user, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @DeleteMapping("/{id}/barcodes")
    @Operation(summary = "Delete barcodes from batch", description = "Delete available barcodes from a batch and decrease stock count. Only available (not sold) barcodes can be deleted.")
    public ResponseEntity<Void> deleteBarcodesFromBatch(
            @PathVariable Long id,
            @RequestParam("barcodeIds") List<Long> barcodeIds,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpRequest) {
        stockBarcodeService.deleteBarcodesFromBatch(id, barcodeIds, user, httpRequest);
        return ResponseEntity.noContent().build();
    }
}

