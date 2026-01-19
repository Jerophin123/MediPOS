package com.medicalstore.pos.service;

import com.medicalstore.pos.dto.request.ReturnItemRequest;
import com.medicalstore.pos.dto.request.ReturnRequest;
import com.medicalstore.pos.dto.response.BillResponse;
import com.medicalstore.pos.dto.response.BillItemResponse;
import com.medicalstore.pos.dto.response.PaymentResponse;
import com.medicalstore.pos.dto.response.ReturnResponse;
import com.medicalstore.pos.dto.response.ReturnItemResponse;
import com.medicalstore.pos.entity.Bill;
import com.medicalstore.pos.entity.BillItem;
import com.medicalstore.pos.entity.AuditLog;
import com.medicalstore.pos.entity.Payment;
import com.medicalstore.pos.entity.Return;
import com.medicalstore.pos.entity.ReturnItem;
import com.medicalstore.pos.entity.Return.ReturnType;
import com.medicalstore.pos.entity.User;
import com.medicalstore.pos.repository.BillRepository;
import com.medicalstore.pos.repository.ReturnItemRepository;
import com.medicalstore.pos.repository.ReturnRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReturnService {
    
    private final ReturnRepository returnRepository;
    private final ReturnItemRepository returnItemRepository;
    private final BillRepository billRepository;
    private final BatchService batchService;
    private final AuditService auditService;
    
    public ReturnService(ReturnRepository returnRepository, ReturnItemRepository returnItemRepository,
                        BillRepository billRepository, BatchService batchService, AuditService auditService) {
        this.returnRepository = returnRepository;
        this.returnItemRepository = returnItemRepository;
        this.billRepository = billRepository;
        this.batchService = batchService;
        this.auditService = auditService;
    }
    
    /**
     * Processes a return and restores stock to the ORIGINAL batch.
     */
    @Transactional(rollbackFor = Exception.class)
    public BillResponse processReturn(ReturnRequest request, User user, HttpServletRequest httpRequest) {
        // Get the bill entity - use getReferenceById to get a managed proxy
        // This ensures the entity is properly managed in the persistence context
        if (!billRepository.existsById(request.getBillId())) {
            throw new RuntimeException("Bill not found with id: " + request.getBillId());
        }
        
        // Use getReferenceById to get a managed entity reference (proxy)
        // This is better for foreign key relationships as it doesn't load the full entity
        Bill originalBill = billRepository.getReferenceById(request.getBillId());
        
        // Verify bill has an ID by accessing it (this will trigger proxy initialization if needed)
        Long billId = originalBill.getId();
        if (billId == null) {
            throw new RuntimeException("Bill ID is null");
        }
        
        // Force load bill items and payments within transaction to avoid LazyInitializationException
        originalBill.getBillItems().size();
        originalBill.getPayments().size();
        
        if (originalBill.getCancelled()) {
            throw new RuntimeException("Cannot process return for a cancelled bill");
        }
        
        if (originalBill.getPaymentStatus() != Bill.PaymentStatus.PAID) {
            throw new RuntimeException("Can only process returns for paid bills");
        }
        
        // Generate return number
        String returnNumber = generateReturnNumber();
        
        // Process return items and calculate refund amount
        BigDecimal totalRefund = BigDecimal.ZERO;
        ReturnType returnType = ReturnType.FULL;
        List<ReturnItem> returnItems = new ArrayList<>();
        
        for (ReturnItemRequest itemRequest : request.getItems()) {
            BillItem billItem = originalBill.getBillItems().stream()
                    .filter(item -> item.getId().equals(itemRequest.getBillItemId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Bill item not found: " + itemRequest.getBillItemId()));
            
            if (itemRequest.getQuantity() > billItem.getQuantity()) {
                throw new RuntimeException("Return quantity cannot exceed original quantity");
            }
            
            // Calculate refund amount (proportional)
            BigDecimal refundPerUnit = billItem.getTotalAmount()
                    .divide(BigDecimal.valueOf(billItem.getQuantity()), 2, java.math.RoundingMode.HALF_UP);
            BigDecimal itemRefund = refundPerUnit.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalRefund = totalRefund.add(itemRefund);
            
            // Restore stock to ORIGINAL batch
            batchService.restoreStock(billItem.getBatch().getId(), itemRequest.getQuantity());
            
            if (itemRequest.getQuantity() < billItem.getQuantity()) {
                returnType = ReturnType.PARTIAL;
            }
        }
        
        // Create return entity - ensure originalBill is properly set
        // Validate that originalBill is not null and has an ID
        if (originalBill == null || originalBill.getId() == null) {
            throw new RuntimeException("Invalid bill entity: bill is null or has no ID");
        }
        
        // Create return entity using constructor and setters to ensure relationships are set
        // Using setters instead of builder to avoid any Lombok builder issues with relationships
        Return returnEntity = new Return();
        returnEntity.setReturnNumber(returnNumber);
        returnEntity.setOriginalBill(originalBill);  // Set the managed bill entity
        returnEntity.setProcessedBy(user);  // Set the user entity
        returnEntity.setReturnDate(LocalDateTime.now());
        returnEntity.setRefundAmount(totalRefund);
        returnEntity.setReason(request.getReason());
        returnEntity.setReturnType(returnType);
        
        // Double-check relationships are set
        if (returnEntity.getOriginalBill() == null) {
            throw new RuntimeException("originalBill is null after setting. Bill ID was: " + billId);
        }
        if (returnEntity.getOriginalBill().getId() == null) {
            throw new RuntimeException("originalBill.getId() is null. Bill entity: " + originalBill);
        }
        if (returnEntity.getProcessedBy() == null) {
            throw new RuntimeException("processedBy is null after setting. User ID was: " + 
                    (user != null ? user.getId() : "null"));
        }
        if (returnEntity.getProcessedBy().getId() == null) {
            throw new RuntimeException("processedBy.getId() is null. User entity: " + user);
        }
        
        // Log for debugging
        System.out.println("DEBUG: Before save - originalBill.id=" + returnEntity.getOriginalBill().getId() + 
                          ", processedBy.id=" + returnEntity.getProcessedBy().getId());
        
        // Save the entity
        returnEntity = returnRepository.saveAndFlush(returnEntity);  // Use saveAndFlush to force immediate persistence
        
        // Create return items with proper reference
        for (ReturnItemRequest itemRequest : request.getItems()) {
            BillItem billItem = originalBill.getBillItems().stream()
                    .filter(item -> item.getId().equals(itemRequest.getBillItemId()))
                    .findFirst()
                    .orElseThrow();
            
            BigDecimal refundPerUnit = billItem.getTotalAmount()
                    .divide(BigDecimal.valueOf(billItem.getQuantity()), 2, java.math.RoundingMode.HALF_UP);
            BigDecimal itemRefund = refundPerUnit.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            
            ReturnItem returnItem = ReturnItem.builder()
                    .returnEntity(returnEntity)
                    .medicine(billItem.getMedicine())
                    .batch(billItem.getBatch())
                    .batchNumber(billItem.getBatchNumber())
                    .quantity(itemRequest.getQuantity())
                    .refundAmount(itemRefund)
                    .build();
            
            returnItems.add(returnItem);
        }
        
        // Save return items
        returnItemRepository.saveAll(returnItems);
        
        // Update bill payment status if full return
        if (returnType == ReturnType.FULL) {
            originalBill.setPaymentStatus(Bill.PaymentStatus.REFUNDED);
            billRepository.save(originalBill);
        }
        
        // Audit log
        auditService.log(AuditLog.ActionType.REFUND_PROCESSED, user, "Return", 
                        returnEntity.getId().toString(), "Return processed: " + returnNumber,
                        null, returnEntity.toString(), httpRequest);
        
        // Return updated bill
        return mapBillToResponse(originalBill);
    }
    
    private String generateReturnNumber() {
        LocalDateTime now = LocalDateTime.now();
        String prefix = "RET" + now.getYear() + String.format("%02d", now.getMonthValue()) + 
                       String.format("%02d", now.getDayOfMonth());
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private BillResponse mapBillToResponse(Bill bill) {
        // Map bill items
        List<BillItemResponse> items = bill.getBillItems().stream()
                .map(this::mapItemToResponse)
                .collect(Collectors.toList());
        
        // Map payments
        List<PaymentResponse> payments = bill.getPayments().stream()
                .map(this::mapPaymentToResponse)
                .collect(Collectors.toList());
        
        // Recalculate payment status based on actual payments
        Bill.PaymentStatus calculatedStatus = calculatePaymentStatus(bill);
        
        return BillResponse.builder()
                .id(bill.getId())
                .billNumber(bill.getBillNumber())
                .billDate(bill.getBillDate())
                .cashierId(bill.getCashier().getId())
                .cashierName(bill.getCashier().getFullName())
                .customerName(bill.getCustomerName())
                .customerPhone(bill.getCustomerPhone())
                .subtotal(bill.getSubtotal())
                .totalGst(bill.getTotalGst())
                .totalAmount(bill.getTotalAmount())
                .paymentStatus(calculatedStatus)
                .cancelled(bill.getCancelled())
                .cancellationReason(bill.getCancellationReason())
                .items(items)
                .payments(payments)
                .createdAt(bill.getCreatedAt())
                .build();
    }
    
    /**
     * Calculates payment status based on total paid amount vs bill total
     */
    private Bill.PaymentStatus calculatePaymentStatus(Bill bill) {
        if (bill.getCancelled()) {
            return bill.getPaymentStatus(); // Keep original status for cancelled bills
        }
        
        BigDecimal totalPaid = bill.getPayments().stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.COMPLETED)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (totalPaid.compareTo(bill.getTotalAmount()) < 0) {
            return Bill.PaymentStatus.PARTIALLY_PAID;
        } else {
            return Bill.PaymentStatus.PAID;
        }
    }
    
    private BillItemResponse mapItemToResponse(BillItem item) {
        return BillItemResponse.builder()
                .id(item.getId())
                .medicineId(item.getMedicine().getId())
                .medicineName(item.getMedicine().getName())
                .batchNumber(item.getBatchNumber())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .gstPercentage(item.getGstPercentage())
                .gstAmount(item.getGstAmount())
                .totalAmount(item.getTotalAmount())
                .build();
    }
    
    private PaymentResponse mapPaymentToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .paymentReference(payment.getPaymentReference())
                .mode(payment.getMode())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .paymentDate(payment.getPaymentDate())
                .build();
    }
    
    /**
     * Get all returns
     */
    @Transactional(readOnly = true)
    public List<ReturnResponse> getAllReturns() {
        try {
            List<Return> returns = returnRepository.findAll();
            if (returns == null || returns.isEmpty()) {
                return new ArrayList<>();
            }
            // Force load all relationships before mapping
            for (Return returnEntity : returns) {
                if (returnEntity.getOriginalBill() != null) {
                    returnEntity.getOriginalBill().getBillNumber();
                }
                if (returnEntity.getProcessedBy() != null) {
                    returnEntity.getProcessedBy().getFullName();
                }
            }
            return returns.stream()
                    .map(this::mapReturnToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error fetching returns: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get return by ID
     */
    @Transactional(readOnly = true)
    public ReturnResponse getReturnById(Long id) {
        Return returnEntity = returnRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Return not found with id: " + id));
        return mapReturnToResponse(returnEntity);
    }
    
    /**
     * Get returns by bill ID
     */
    @Transactional(readOnly = true)
    public List<ReturnResponse> getReturnsByBillId(Long billId) {
        List<Return> returns = returnRepository.findByOriginalBillId(billId);
        return returns.stream()
                .map(this::mapReturnToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Map Return entity to ReturnResponse DTO
     */
    private ReturnResponse mapReturnToResponse(Return returnEntity) {
        try {
            // Force load lazy relationships within transaction
            if (returnEntity.getOriginalBill() != null) {
                returnEntity.getOriginalBill().getBillNumber();
            }
            if (returnEntity.getProcessedBy() != null) {
                returnEntity.getProcessedBy().getFullName();
            }
            
            // Load return items (JOIN FETCH should eagerly load medicine and batch)
            List<ReturnItem> returnItems = returnItemRepository.findByReturnEntity(returnEntity);
            
            List<ReturnItemResponse> items = (returnItems != null) 
                    ? returnItems.stream()
                            .map(this::mapReturnItemToResponse)
                            .collect(Collectors.toList())
                    : new ArrayList<>();
            
            return ReturnResponse.builder()
                    .id(returnEntity.getId())
                    .returnNumber(returnEntity.getReturnNumber())
                    .billId(returnEntity.getOriginalBill() != null ? returnEntity.getOriginalBill().getId() : null)
                    .billNumber(returnEntity.getOriginalBill() != null ? returnEntity.getOriginalBill().getBillNumber() : null)
                    .processedById(returnEntity.getProcessedBy() != null ? returnEntity.getProcessedBy().getId() : null)
                    .processedByName(returnEntity.getProcessedBy() != null ? returnEntity.getProcessedBy().getFullName() : null)
                    .returnDate(returnEntity.getReturnDate())
                    .refundAmount(returnEntity.getRefundAmount())
                    .reason(returnEntity.getReason())
                    .returnType(returnEntity.getReturnType())
                    .createdAt(returnEntity.getCreatedAt())
                    .items(items)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Error mapping return to response: " + e.getMessage(), e);
        }
    }
    
    /**
     * Map ReturnItem entity to ReturnItemResponse DTO
     */
    private ReturnItemResponse mapReturnItemToResponse(ReturnItem returnItem) {
        // Ensure lazy relationships are loaded (JOIN FETCH should handle this, but be safe)
        if (returnItem.getMedicine() != null) {
            returnItem.getMedicine().getName(); // Force load
        }
        if (returnItem.getBatch() != null) {
            returnItem.getBatch().getId(); // Force load
        }
        
        return ReturnItemResponse.builder()
                .id(returnItem.getId())
                .medicineId(returnItem.getMedicine() != null ? returnItem.getMedicine().getId() : null)
                .medicineName(returnItem.getMedicine() != null ? returnItem.getMedicine().getName() : null)
                .batchId(returnItem.getBatch() != null ? returnItem.getBatch().getId() : null)
                .batchNumber(returnItem.getBatchNumber())
                .quantity(returnItem.getQuantity())
                .refundAmount(returnItem.getRefundAmount())
                .build();
    }
}
