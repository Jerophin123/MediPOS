package com.medicalstore.pos.service;

import com.medicalstore.pos.dto.request.AddBarcodesRequest;
import com.medicalstore.pos.dto.response.MedicineResponse;
import com.medicalstore.pos.dto.response.StockBarcodeResponse;
import com.medicalstore.pos.entity.AuditLog;
import com.medicalstore.pos.entity.Batch;
import com.medicalstore.pos.entity.StockBarcode;
import com.medicalstore.pos.entity.User;
import com.medicalstore.pos.repository.StockBarcodeRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StockBarcodeService {
    
    private final StockBarcodeRepository stockBarcodeRepository;
    private final BatchService batchService;
    private final MedicineService medicineService;
    private final AuditService auditService;
    
    public StockBarcodeService(StockBarcodeRepository stockBarcodeRepository,
                              BatchService batchService,
                              MedicineService medicineService,
                              AuditService auditService) {
        this.stockBarcodeRepository = stockBarcodeRepository;
        this.batchService = batchService;
        this.medicineService = medicineService;
        this.auditService = auditService;
    }
    
    /**
     * Find medicine by barcode for real-time scanning
     * Returns the medicine associated with the barcode's batch
     */
    @Transactional(readOnly = true)
    public MedicineResponse findMedicineByBarcode(String barcode) {
        StockBarcode stockBarcode = stockBarcodeRepository.findAvailableByBarcode(barcode)
                .orElseThrow(() -> new RuntimeException("Barcode not found or already sold: " + barcode));
        
        Batch batch = stockBarcode.getBatch();
        
        // Check if batch is expired
        if (batch.isExpired()) {
            throw new RuntimeException("Medicine with barcode " + barcode + " has expired");
        }
        
        // Check if batch has available stock
        if (batch.getQuantityAvailable() <= 0) {
            throw new RuntimeException("Medicine with barcode " + barcode + " is out of stock");
        }
        
        return medicineService.getMedicineById(batch.getMedicine().getId());
    }
    
    /**
     * Get batch for a barcode (used during billing)
     */
    @Transactional(readOnly = true)
    public Batch getBatchByBarcode(String barcode) {
        StockBarcode stockBarcode = stockBarcodeRepository.findAvailableByBarcode(barcode)
                .orElseThrow(() -> new RuntimeException("Barcode not found or already sold: " + barcode));
        
        Batch batch = stockBarcode.getBatch();
        
        if (batch.isExpired()) {
            throw new RuntimeException("Medicine with barcode " + barcode + " has expired");
        }
        
        if (batch.getQuantityAvailable() <= 0) {
            throw new RuntimeException("Medicine with barcode " + barcode + " is out of stock");
        }
        
        return batch;
    }
    
    /**
     * Mark barcode as sold (called after successful payment)
     */
    @Transactional
    public void markBarcodeAsSold(String barcode) {
        StockBarcode stockBarcode = stockBarcodeRepository.findAvailableByBarcode(barcode)
                .orElseThrow(() -> new RuntimeException("Barcode not found or already sold: " + barcode));
        
        stockBarcode.setSold(true);
        stockBarcodeRepository.save(stockBarcode);
    }
    
    /**
     * Mark barcode as available (for returns/refunds)
     */
    @Transactional
    public void markBarcodeAsAvailable(String barcode) {
        StockBarcode stockBarcode = stockBarcodeRepository.findByBarcode(barcode)
                .orElseThrow(() -> new RuntimeException("Barcode not found: " + barcode));
        
        if (!stockBarcode.getSold()) {
            throw new RuntimeException("Barcode " + barcode + " is already available");
        }
        
        stockBarcode.setSold(false);
        stockBarcodeRepository.save(stockBarcode);
    }
    
    /**
     * Get all barcodes for a batch
     */
    @Transactional(readOnly = true)
    public List<StockBarcode> getBarcodesByBatch(Batch batch) {
        return stockBarcodeRepository.findByBatch(batch);
    }
    
    /**
     * Get available barcodes for a batch
     */
    @Transactional(readOnly = true)
    public List<StockBarcode> getAvailableBarcodesByBatch(Batch batch) {
        return stockBarcodeRepository.findByBatchAndSold(batch, false);
    }
    
    /**
     * Search medicines by barcode prefix (for incremental search as user types)
     * Returns unique medicines that have available barcodes matching the prefix
     */
    @Transactional(readOnly = true)
    public List<MedicineResponse> searchMedicinesByBarcodePrefix(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return List.of();
        }
        
        try {
            List<StockBarcode> matchingBarcodes = stockBarcodeRepository.findAvailableByBarcodePrefix(prefix.trim());
            
            if (matchingBarcodes.isEmpty()) {
                return List.of();
            }
            
            // Get unique medicines from the matching barcodes using a Set to ensure uniqueness
            return matchingBarcodes.stream()
                    .map(StockBarcode::getBatch)
                    .map(Batch::getMedicine)
                    .distinct()
                    .map(medicine -> medicineService.getMedicineById(medicine.getId()))
                    .toList();
        } catch (Exception e) {
            // Log error and return empty list
            System.err.println("Error searching barcodes by prefix: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }
    
    /**
     * Get all barcodes for a batch (including sold ones)
     */
    @Transactional(readOnly = true)
    public List<StockBarcodeResponse> getBarcodesByBatchId(Long batchId) {
        Batch batch = batchService.getBatchEntity(batchId);
        List<StockBarcode> barcodes = stockBarcodeRepository.findByBatch(batch);
        return barcodes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Add new barcodes to a batch and update stock count
     */
    @Transactional
    public List<StockBarcodeResponse> addBarcodesToBatch(Long batchId, AddBarcodesRequest request, User user, HttpServletRequest httpRequest) {
        Batch batch = batchService.getBatchEntity(batchId);
        
        List<StockBarcode> newBarcodes = new java.util.ArrayList<>();
        for (String barcode : request.getBarcodes()) {
            if (barcode == null || barcode.trim().isEmpty()) {
                throw new RuntimeException("Barcode cannot be empty");
            }
            
            // Check for duplicate barcodes
            if (stockBarcodeRepository.findByBarcode(barcode.trim()).isPresent()) {
                throw new RuntimeException("Barcode " + barcode.trim() + " already exists");
            }
            
            StockBarcode stockBarcode = StockBarcode.builder()
                    .batch(batch)
                    .barcode(barcode.trim())
                    .sold(false)
                    .build();
            newBarcodes.add(stockBarcode);
        }
        
        stockBarcodeRepository.saveAll(newBarcodes);
        
        // Update batch quantity
        batch.setQuantityAvailable(batch.getQuantityAvailable() + newBarcodes.size());
        batchService.saveBatch(batch);
        
        // Audit log
        auditService.log(AuditLog.ActionType.STOCK_UPDATED, user, "Batch",
                batch.getId().toString(), "Added " + newBarcodes.size() + " barcodes to batch: " + batch.getBatchNumber(),
                String.valueOf(batch.getQuantityAvailable() - newBarcodes.size()),
                String.valueOf(batch.getQuantityAvailable()), httpRequest);
        
        return newBarcodes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Delete barcodes from a batch and decrease stock count
     * Only deletes available (not sold) barcodes
     */
    @Transactional
    public void deleteBarcodesFromBatch(Long batchId, List<Long> barcodeIds, User user, HttpServletRequest httpRequest) {
        Batch batch = batchService.getBatchEntity(batchId);
        
        List<StockBarcode> barcodesToDelete = stockBarcodeRepository.findAllById(barcodeIds);
        
        // Filter to only delete available (not sold) barcodes
        List<StockBarcode> availableBarcodes = barcodesToDelete.stream()
                .filter(b -> !b.getSold() && b.getBatch().getId().equals(batchId))
                .collect(Collectors.toList());
        
        if (availableBarcodes.isEmpty()) {
            throw new RuntimeException("No available barcodes found to delete. Only available (not sold) barcodes can be deleted.");
        }
        
        // Delete barcodes
        stockBarcodeRepository.deleteAll(availableBarcodes);
        
        // Update batch quantity
        int oldQuantity = batch.getQuantityAvailable();
        batch.setQuantityAvailable(batch.getQuantityAvailable() - availableBarcodes.size());
        batchService.saveBatch(batch);
        
        // Audit log
        auditService.log(AuditLog.ActionType.STOCK_UPDATED, user, "Batch",
                batch.getId().toString(), "Deleted " + availableBarcodes.size() + " barcodes from batch: " + batch.getBatchNumber(),
                String.valueOf(oldQuantity),
                String.valueOf(batch.getQuantityAvailable()), httpRequest);
    }
    
    /**
     * Map StockBarcode entity to StockBarcodeResponse DTO
     */
    private StockBarcodeResponse mapToResponse(StockBarcode stockBarcode) {
        return StockBarcodeResponse.builder()
                .id(stockBarcode.getId())
                .batchId(stockBarcode.getBatch().getId())
                .barcode(stockBarcode.getBarcode())
                .sold(stockBarcode.getSold())
                .createdAt(stockBarcode.getCreatedAt())
                .updatedAt(stockBarcode.getUpdatedAt())
                .build();
    }
}

