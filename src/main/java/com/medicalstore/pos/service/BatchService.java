package com.medicalstore.pos.service;

import com.medicalstore.pos.dto.request.CreateBatchRequest;
import com.medicalstore.pos.dto.request.UpdateBatchRequest;
import com.medicalstore.pos.dto.request.UpdateStockRequest;
import com.medicalstore.pos.dto.response.BatchResponse;
import com.medicalstore.pos.entity.AuditLog;
import com.medicalstore.pos.entity.Batch;
import com.medicalstore.pos.entity.Medicine;
import com.medicalstore.pos.entity.StockBarcode;
import com.medicalstore.pos.entity.User;
import com.medicalstore.pos.repository.BatchRepository;
import com.medicalstore.pos.repository.StockBarcodeRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BatchService {
    
    private final BatchRepository batchRepository;
    private final StockBarcodeRepository stockBarcodeRepository;
    private final MedicineService medicineService;
    private final AuditService auditService;
    
    public BatchService(BatchRepository batchRepository, 
                       StockBarcodeRepository stockBarcodeRepository,
                       MedicineService medicineService, 
                       AuditService auditService) {
        this.batchRepository = batchRepository;
        this.stockBarcodeRepository = stockBarcodeRepository;
        this.medicineService = medicineService;
        this.auditService = auditService;
    }
    
    @Transactional
    public BatchResponse createBatch(CreateBatchRequest request, User user, HttpServletRequest httpRequest) {
        Medicine medicine = medicineService.getMedicineEntity(request.getMedicineId());
        
        if (request.getExpiryDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Expiry date cannot be in the past");
        }
        
        Batch batch = Batch.builder()
                .medicine(medicine)
                .batchNumber(request.getBatchNumber())
                .expiryDate(request.getExpiryDate())
                .purchasePrice(request.getPurchasePrice())
                .sellingPrice(request.getSellingPrice())
                .quantityAvailable(request.getQuantityAvailable())
                .build();
        
        batch = batchRepository.save(batch);
        
        // Create individual barcodes if provided
        if (request.getBarcodes() != null && !request.getBarcodes().isEmpty()) {
            if (request.getBarcodes().size() != request.getQuantityAvailable()) {
                throw new RuntimeException("Number of barcodes must match quantity available. " +
                        "Expected: " + request.getQuantityAvailable() + ", Provided: " + request.getBarcodes().size());
            }
            
            List<StockBarcode> stockBarcodes = new ArrayList<>();
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
                stockBarcodes.add(stockBarcode);
            }
            
            stockBarcodeRepository.saveAll(stockBarcodes);
        }
        
        auditService.log(AuditLog.ActionType.BATCH_ADDED, user, "Batch", 
                        batch.getId().toString(), "Batch created: " + batch.getBatchNumber(),
                        null, batch.toString(), httpRequest);
        
        return mapToResponse(batch);
    }
    
    @Transactional(readOnly = true)
    public List<BatchResponse> getBatchesByMedicine(Long medicineId) {
        Medicine medicine = medicineService.getMedicineEntity(medicineId);
        return batchRepository.findByMedicine(medicine).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<BatchResponse> getExpiredBatches() {
        return batchRepository.findExpiredBatches(LocalDate.now()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<BatchResponse> getLowStockBatches(Integer threshold) {
        return batchRepository.findLowStockBatches(threshold).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<BatchResponse> getAllBatches() {
        return batchRepository.findAllOrderByCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Batch getAvailableBatchForMedicine(Medicine medicine, Integer quantity) {
        List<Batch> batches = batchRepository.findAvailableBatchesByMedicineOrderByExpiry(
                medicine, LocalDate.now());
        
        // FIFO: Find first batch with sufficient stock
        for (Batch batch : batches) {
            if (batch.hasStock(quantity)) {
                return batch;
            }
        }
        
        // If no single batch has enough, check total available
        Integer totalAvailable = batchRepository.getTotalAvailableQuantity(medicine, LocalDate.now());
        if (totalAvailable < quantity) {
            throw new RuntimeException("Insufficient stock for medicine: " + medicine.getName() + 
                    ". Available: " + totalAvailable + ", Required: " + quantity);
        }
        
        // Return first batch (will be handled by allocating from multiple batches if needed)
        if (batches.isEmpty()) {
            throw new RuntimeException("No available batches found for medicine: " + medicine.getName());
        }
        
        return batches.get(0);
    }
    
    @Transactional
    public void deductStock(Long batchId, Integer quantity) {
        Batch batch = batchRepository.findByIdForUpdate(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found: " + batchId));
        
        if (!batch.hasStock(quantity)) {
            throw new RuntimeException("Insufficient stock in batch: " + batch.getBatchNumber() + 
                    ". Available: " + batch.getQuantityAvailable() + ", Required: " + quantity);
        }
        
        batch.setQuantityAvailable(batch.getQuantityAvailable() - quantity);
        batchRepository.save(batch);
    }
    
    @Transactional
    public void restoreStock(Long batchId, Integer quantity) {
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found: " + batchId));
        
        batch.setQuantityAvailable(batch.getQuantityAvailable() + quantity);
        batchRepository.save(batch);
    }
    
    @Transactional(readOnly = true)
    public Batch getBatchEntity(Long id) {
        return batchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Batch not found with id: " + id));
    }
    
    @Transactional(readOnly = true)
    public BatchResponse getBatchById(Long id) {
        Batch batch = batchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Batch not found with id: " + id));
        return mapToResponse(batch);
    }
    
    @Transactional
    public Batch saveBatch(Batch batch) {
        return batchRepository.save(batch);
    }
    
    @Transactional
    public BatchResponse updateBatch(Long id, UpdateBatchRequest request, User user, HttpServletRequest httpRequest) {
        Batch batch = batchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Batch not found with id: " + id));
        
        if (request.getExpiryDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Expiry date cannot be in the past");
        }
        
        String oldValue = batch.toString();
        
        batch.setBatchNumber(request.getBatchNumber());
        batch.setExpiryDate(request.getExpiryDate());
        batch.setPurchasePrice(request.getPurchasePrice());
        batch.setSellingPrice(request.getSellingPrice());
        batch.setQuantityAvailable(request.getQuantityAvailable());
        
        batch = batchRepository.save(batch);
        
        auditService.log(AuditLog.ActionType.BATCH_UPDATED, user, "Batch", 
                        batch.getId().toString(), "Batch updated: " + batch.getBatchNumber(),
                        oldValue, batch.toString(), httpRequest);
        
        return mapToResponse(batch);
    }
    
    @Transactional
    public BatchResponse updateStock(Long id, UpdateStockRequest request, User user, HttpServletRequest httpRequest) {
        Batch batch = batchRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new RuntimeException("Batch not found with id: " + id));
        
        Integer oldQuantity = batch.getQuantityAvailable();
        batch.setQuantityAvailable(request.getQuantityAvailable());
        batch = batchRepository.save(batch);
        
        auditService.log(AuditLog.ActionType.STOCK_UPDATED, user, "Batch", 
                        batch.getId().toString(), "Stock updated for batch: " + batch.getBatchNumber(),
                        oldQuantity.toString(), request.getQuantityAvailable().toString(), httpRequest);
        
        return mapToResponse(batch);
    }
    
    @Transactional
    public void deleteBatch(Long id, User user, HttpServletRequest httpRequest) {
        Batch batch = batchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Batch not found with id: " + id));
        
        // Check if batch has been used in bills (if quantity available is less than what was initially added)
        // Note: We can't check initialQuantity directly, so we'll allow deletion if quantity is 0
        // In a real system, you'd track initial quantity separately
        if (batch.getQuantityAvailable() > 0) {
            throw new RuntimeException("Cannot delete batch with available stock. Please set quantity to 0 first or use the stock.");
        }
        
        String batchInfo = batch.toString();
        batchRepository.delete(batch);
        
        auditService.log(AuditLog.ActionType.BATCH_DELETED, user, "Batch", 
                        id.toString(), "Batch deleted: " + batch.getBatchNumber(),
                        batchInfo, null, httpRequest);
    }
    
    BatchResponse mapToResponse(Batch batch) {
        return BatchResponse.builder()
                .id(batch.getId())
                .medicineId(batch.getMedicine().getId())
                .medicineName(batch.getMedicine().getName())
                .batchNumber(batch.getBatchNumber())
                .expiryDate(batch.getExpiryDate())
                .purchasePrice(batch.getPurchasePrice())
                .sellingPrice(batch.getSellingPrice())
                .quantityAvailable(batch.getQuantityAvailable())
                .expired(batch.isExpired())
                .createdAt(batch.getCreatedAt())
                .updatedAt(batch.getUpdatedAt())
                .build();
    }
}

