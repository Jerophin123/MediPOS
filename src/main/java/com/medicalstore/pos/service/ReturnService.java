package com.medicalstore.pos.service;

import com.medicalstore.pos.dto.request.ReturnItemRequest;
import com.medicalstore.pos.dto.request.ReturnRequest;
import com.medicalstore.pos.dto.response.BillResponse;
import com.medicalstore.pos.entity.Bill;
import com.medicalstore.pos.entity.BillItem;
import com.medicalstore.pos.entity.AuditLog;
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
        Bill originalBill = billRepository.findById(request.getBillId())
                .orElseThrow(() -> new RuntimeException("Bill not found with id: " + request.getBillId()));
        
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
        
        // Create return entity
        Return returnEntity = Return.builder()
                .returnNumber(returnNumber)
                .originalBill(originalBill)
                .processedBy(user)
                .returnDate(LocalDateTime.now())
                .refundAmount(totalRefund)
                .reason(request.getReason())
                .returnType(returnType)
                .build();
        
        returnEntity = returnRepository.save(returnEntity);
        
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
        // Simplified mapping - in production, use proper mapper
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
                .paymentStatus(bill.getPaymentStatus())
                .cancelled(bill.getCancelled())
                .cancellationReason(bill.getCancellationReason())
                .createdAt(bill.getCreatedAt())
                .build();
    }
}

