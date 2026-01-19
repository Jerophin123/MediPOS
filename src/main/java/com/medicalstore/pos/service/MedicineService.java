package com.medicalstore.pos.service;

import com.medicalstore.pos.dto.request.CreateMedicineRequest;
import com.medicalstore.pos.dto.request.UpdateMedicineRequest;
import com.medicalstore.pos.dto.response.MedicineResponse;
import com.medicalstore.pos.entity.AuditLog;
import com.medicalstore.pos.entity.Medicine;
import com.medicalstore.pos.entity.User;
import com.medicalstore.pos.repository.BatchRepository;
import com.medicalstore.pos.repository.MedicineRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MedicineService {
    
    private final MedicineRepository medicineRepository;
    private final BatchRepository batchRepository;
    private final BatchService batchService;
    private final AuditService auditService;
    private static final int DEFAULT_LOW_STOCK_THRESHOLD = 10;
    
    public MedicineService(MedicineRepository medicineRepository, 
                         BatchRepository batchRepository,
                         @Lazy BatchService batchService,
                         AuditService auditService) {
        this.medicineRepository = medicineRepository;
        this.batchRepository = batchRepository;
        this.batchService = batchService;
        this.auditService = auditService;
    }
    
    @Transactional
    public MedicineResponse createMedicine(CreateMedicineRequest request, User user, HttpServletRequest httpRequest) {
        if (medicineRepository.findByHsnCode(request.getHsnCode()).isPresent()) {
            throw new RuntimeException("Medicine with HSN code " + request.getHsnCode() + " already exists");
        }
        
        Medicine medicine = Medicine.builder()
                .name(request.getName())
                .manufacturer(request.getManufacturer())
                .category(request.getCategory())
                .barcode(request.getBarcode())
                .hsnCode(request.getHsnCode())
                .gstPercentage(request.getGstPercentage())
                .prescriptionRequired(request.getPrescriptionRequired())
                .status(Medicine.Status.ACTIVE)
                .build();
        
        medicine = medicineRepository.save(medicine);
        
        // If initial stock and pricing are provided, create a batch automatically
        if (request.getInitialStock() != null && request.getInitialStock() > 0) {
            if (request.getPurchasePrice() == null || request.getSellingPrice() == null) {
                throw new RuntimeException("Purchase price and selling price are required when adding initial stock");
            }
            if (request.getExpiryDate() == null) {
                throw new RuntimeException("Expiry date is required when adding initial stock");
            }
            if (request.getBatchNumber() == null || request.getBatchNumber().trim().isEmpty()) {
                throw new RuntimeException("Batch number is required when adding initial stock");
            }
            
            // Create batch using BatchService
            com.medicalstore.pos.dto.request.CreateBatchRequest batchRequest = 
                new com.medicalstore.pos.dto.request.CreateBatchRequest();
            batchRequest.setMedicineId(medicine.getId());
            batchRequest.setBatchNumber(request.getBatchNumber());
            batchRequest.setExpiryDate(request.getExpiryDate());
            batchRequest.setPurchasePrice(request.getPurchasePrice());
            batchRequest.setSellingPrice(request.getSellingPrice());
            batchRequest.setQuantityAvailable(request.getInitialStock());
            batchRequest.setBarcodes(request.getBarcodes()); // Pass individual barcodes
            
            batchService.createBatch(batchRequest, user, httpRequest);
        }
        
        auditService.log(AuditLog.ActionType.MEDICINE_ADDED, user, "Medicine", 
                        medicine.getId().toString(), "Medicine created: " + medicine.getName(),
                        null, medicine.toString(), httpRequest);
        
        return mapToResponse(medicine);
    }
    
    @Transactional(readOnly = true)
    public MedicineResponse getMedicineById(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicine not found with id: " + id));
        return mapToResponse(medicine);
    }
    
    @Transactional(readOnly = true)
    public List<MedicineResponse> getAllMedicines() {
        List<Medicine> medicines = medicineRepository.findAll();
        return mapToResponseList(medicines);
    }
    
    @Transactional(readOnly = true)
    public List<MedicineResponse> searchMedicines(String searchTerm) {
        List<Medicine> medicines = medicineRepository.findByNameContainingIgnoreCase(searchTerm);
        return mapToResponseList(medicines);
    }
    
    @Transactional
    public MedicineResponse updateMedicineStatus(Long id, Medicine.Status status, User user, HttpServletRequest httpRequest) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicine not found with id: " + id));
        
        Medicine.Status oldStatus = medicine.getStatus();
        medicine.setStatus(status);
        medicine = medicineRepository.save(medicine);
        
        auditService.log(AuditLog.ActionType.MEDICINE_UPDATED, user, "Medicine", 
                        medicine.getId().toString(), "Medicine status updated",
                        oldStatus.toString(), status.toString(), httpRequest);
        
        return mapToResponse(medicine);
    }
    
    @Transactional
    public MedicineResponse updateMedicine(Long id, UpdateMedicineRequest request, User user, HttpServletRequest httpRequest) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicine not found with id: " + id));
        
        // Check if HSN code is being changed and if it conflicts with another medicine
        if (!medicine.getHsnCode().equals(request.getHsnCode())) {
            if (medicineRepository.findByHsnCode(request.getHsnCode()).isPresent()) {
                throw new RuntimeException("Medicine with HSN code " + request.getHsnCode() + " already exists");
            }
        }
        
        String oldValue = medicine.toString();
        
        medicine.setName(request.getName());
        medicine.setManufacturer(request.getManufacturer());
        medicine.setCategory(request.getCategory());
        medicine.setBarcode(request.getBarcode());
        medicine.setHsnCode(request.getHsnCode());
        medicine.setGstPercentage(request.getGstPercentage());
        medicine.setPrescriptionRequired(request.getPrescriptionRequired());
        
        medicine = medicineRepository.save(medicine);
        
        auditService.log(AuditLog.ActionType.MEDICINE_UPDATED, user, "Medicine", 
                        medicine.getId().toString(), "Medicine updated: " + medicine.getName(),
                        oldValue, medicine.toString(), httpRequest);
        
        return mapToResponse(medicine);
    }
    
    @Transactional
    public void deleteMedicine(Long id, User user, HttpServletRequest httpRequest) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicine not found with id: " + id));
        
        // Check if medicine has any batches - need to check via repository
        long batchCount = medicineRepository.countBatchesByMedicineId(id);
        if (batchCount > 0) {
            throw new RuntimeException("Cannot delete medicine with existing batches. Please delete batches first.");
        }
        
        String medicineInfo = medicine.toString();
        medicineRepository.delete(medicine);
        
        auditService.log(AuditLog.ActionType.MEDICINE_DELETED, user, "Medicine", 
                        id.toString(), "Medicine deleted: " + medicine.getName(),
                        medicineInfo, null, httpRequest);
    }
    
    @Transactional(readOnly = true)
    public Medicine getMedicineEntity(Long id) {
        return medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicine not found with id: " + id));
    }
    
    /**
     * Find medicine by barcode (GTIN/EAN).
     * Barcode identifies the product, not individual units.
     */
    @Transactional(readOnly = true)
    public MedicineResponse findMedicineByBarcode(String barcode) {
        if (barcode == null || barcode.trim().isEmpty()) {
            throw new RuntimeException("Barcode is required");
        }
        
        Medicine medicine = medicineRepository.findByBarcode(barcode.trim())
                .orElseThrow(() -> new RuntimeException("Medicine not found with barcode: " + barcode));
        
        return mapToResponse(medicine);
    }
    
    /**
     * Search medicines by barcode prefix (for incremental search).
     */
    @Transactional(readOnly = true)
    public List<MedicineResponse> searchMedicinesByBarcodePrefix(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return List.of();
        }
        
        // Find medicines where barcode starts with prefix (case-insensitive)
        List<Medicine> medicines = medicineRepository.findAll().stream()
                .filter(m -> m.getBarcode() != null && 
                        m.getBarcode().toUpperCase().startsWith(prefix.trim().toUpperCase()))
                .toList();
        
        return mapToResponseList(medicines);
    }
    
    /**
     * Map single medicine to response with real-time stock information
     */
    private MedicineResponse mapToResponse(Medicine medicine) {
        LocalDate today = LocalDate.now();
        
        // Get total stock (all batches)
        Integer totalStock = batchRepository.getTotalStockQuantity(medicine);
        if (totalStock == null) {
            totalStock = 0;
        }
        
        // Get available stock (non-expired batches only)
        Integer availableStock = batchRepository.getTotalAvailableQuantity(medicine, today);
        if (availableStock == null) {
            availableStock = 0;
        }
        
        // Determine stock status
        boolean outOfStock = availableStock == 0;
        boolean lowStock = !outOfStock && availableStock <= DEFAULT_LOW_STOCK_THRESHOLD;
        
        return MedicineResponse.builder()
                .id(medicine.getId())
                .name(medicine.getName())
                .manufacturer(medicine.getManufacturer())
                .category(medicine.getCategory())
                .barcode(medicine.getBarcode())
                .hsnCode(medicine.getHsnCode())
                .gstPercentage(medicine.getGstPercentage())
                .prescriptionRequired(medicine.getPrescriptionRequired())
                .status(medicine.getStatus())
                .totalStock(totalStock)
                .availableStock(availableStock)
                .lowStock(lowStock)
                .outOfStock(outOfStock)
                .lowStockThreshold(DEFAULT_LOW_STOCK_THRESHOLD)
                .createdAt(medicine.getCreatedAt())
                .updatedAt(medicine.getUpdatedAt())
                .build();
    }
    
    /**
     * Map list of medicines to responses with optimized batch stock queries
     * This method uses batch queries for better performance in production
     */
    private List<MedicineResponse> mapToResponseList(List<Medicine> medicines) {
        if (medicines.isEmpty()) {
            return List.of();
        }
        
        LocalDate today = LocalDate.now();
        
        // Batch query for stock quantities (performance optimization)
        List<Long> medicineIds = medicines.stream()
                .map(Medicine::getId)
                .collect(Collectors.toList());
        
        // Get total stock quantities for all medicines in one query
        List<Object[]> stockQuantities = batchRepository.getTotalStockQuantitiesByMedicineIds(medicineIds);
        Map<Long, Integer> totalStockMap = stockQuantities.stream()
                .collect(Collectors.toMap(
                    row -> ((Long) row[0]),
                    row -> ((Number) row[1]).intValue()
                ));
        
        // Get available stock for each medicine (non-expired batches)
        Map<Long, Integer> availableStockMap = medicines.stream()
                .collect(Collectors.toMap(
                    Medicine::getId,
                    medicine -> {
                        Integer stock = batchRepository.getTotalAvailableQuantity(medicine, today);
                        return stock != null ? stock : 0;
                    }
                ));
        
        // Map to responses
        return medicines.stream()
                .map(medicine -> {
                    Long medicineId = medicine.getId();
                    Integer totalStock = totalStockMap.getOrDefault(medicineId, 0);
                    Integer availableStock = availableStockMap.getOrDefault(medicineId, 0);
                    boolean outOfStock = availableStock == 0;
                    boolean lowStock = !outOfStock && availableStock <= DEFAULT_LOW_STOCK_THRESHOLD;
                    
                    return MedicineResponse.builder()
                            .id(medicine.getId())
                            .name(medicine.getName())
                            .manufacturer(medicine.getManufacturer())
                            .category(medicine.getCategory())
                            .barcode(medicine.getBarcode())
                            .hsnCode(medicine.getHsnCode())
                            .gstPercentage(medicine.getGstPercentage())
                            .prescriptionRequired(medicine.getPrescriptionRequired())
                            .status(medicine.getStatus())
                            .totalStock(totalStock)
                            .availableStock(availableStock)
                            .lowStock(lowStock)
                            .outOfStock(outOfStock)
                            .lowStockThreshold(DEFAULT_LOW_STOCK_THRESHOLD)
                            .createdAt(medicine.getCreatedAt())
                            .updatedAt(medicine.getUpdatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }
}

