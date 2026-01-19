package com.medicalstore.pos.service;

import com.medicalstore.pos.dto.request.BillItemRequest;
import com.medicalstore.pos.dto.request.CreateBillRequest;
import com.medicalstore.pos.dto.request.PaymentRequest;
import com.medicalstore.pos.dto.response.BillItemResponse;
import com.medicalstore.pos.dto.response.BillResponse;
import com.medicalstore.pos.dto.response.PaymentResponse;
import com.medicalstore.pos.entity.*;
import com.medicalstore.pos.repository.BillRepository;
import com.medicalstore.pos.repository.PaymentRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BillingService {
    
    private final BillRepository billRepository;
    private final PaymentRepository paymentRepository;
    private final MedicineService medicineService;
    private final BatchService batchService;
    private final AuditService auditService;
    
    public BillingService(BillRepository billRepository, PaymentRepository paymentRepository,
                         MedicineService medicineService, BatchService batchService,
                         AuditService auditService) {
        this.billRepository = billRepository;
        this.paymentRepository = paymentRepository;
        this.medicineService = medicineService;
        this.batchService = batchService;
        this.auditService = auditService;
    }
    
    /**
     * Creates a bill with atomic transaction.
     * Stock is deducted ONLY after payment is successful.
     * Uses pessimistic locking to prevent race conditions.
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    public BillResponse createBill(CreateBillRequest request, User cashier, HttpServletRequest httpRequest) {
        // Generate unique bill number
        String billNumber = generateBillNumber();
        
        // Create bill entity
        Bill bill = Bill.builder()
                .billNumber(billNumber)
                .billDate(LocalDateTime.now())
                .cashier(cashier)
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .paymentStatus(Bill.PaymentStatus.PENDING)
                .cancelled(false)
                .build();
        
        // Process bill items with FIFO batch selection
        List<BillItem> billItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalGst = BigDecimal.ZERO;
        
        for (BillItemRequest itemRequest : request.getItems()) {
            Medicine medicine;
            Batch batch;
            
            // Handle barcode scanning or regular medicine selection
            if (itemRequest.getBarcode() != null && !itemRequest.getBarcode().trim().isEmpty()) {
                // CORRECT FLOW: Barcode identifies medicine product, NOT individual unit
                // Step 1: Find medicine by barcode (GTIN/EAN)
                medicine = medicineService.getMedicineEntity(
                    medicineService.findMedicineByBarcode(itemRequest.getBarcode().trim()).getId()
                );
                
                // Step 2: FIFO batch selection (earliest expiry first, non-expired only)
                batch = batchService.getAvailableBatchForMedicine(medicine, itemRequest.getQuantity());
            } else if (itemRequest.getMedicineId() != null) {
                // Regular medicine selection
                medicine = medicineService.getMedicineEntity(itemRequest.getMedicineId());
                
                // FIFO: Get available batch ordered by expiry date
                batch = batchService.getAvailableBatchForMedicine(medicine, itemRequest.getQuantity());
            } else {
                throw new RuntimeException("Either medicineId or barcode must be provided");
            }
            
            // Lock batch for update (pessimistic locking)
            Batch lockedBatch = batchService.getBatchEntity(batch.getId());
            if (!lockedBatch.hasStock(itemRequest.getQuantity())) {
                throw new RuntimeException("Insufficient stock for " + medicine.getName() + 
                        " in batch " + batch.getBatchNumber());
            }
            
            // Calculate prices and GST
            BigDecimal unitPrice = batch.getSellingPrice();
            BigDecimal itemSubtotal = unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            BigDecimal gstAmount = itemSubtotal.multiply(medicine.getGstPercentage())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal itemTotal = itemSubtotal.add(gstAmount);
            
            // Create bill item
            BillItem billItem = BillItem.builder()
                    .bill(bill)
                    .medicine(medicine)
                    .batch(batch)
                    .batchNumber(batch.getBatchNumber())
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(unitPrice)
                    .gstPercentage(medicine.getGstPercentage())
                    .gstAmount(gstAmount)
                    .totalAmount(itemTotal)
                    .build();
            
            billItems.add(billItem);
            subtotal = subtotal.add(itemSubtotal);
            totalGst = totalGst.add(gstAmount);
        }
        
        bill.setBillItems(billItems);
        bill.setSubtotal(subtotal);
        bill.setTotalGst(totalGst);
        bill.setTotalAmount(subtotal.add(totalGst));
        
        // Process payments
        BigDecimal totalPaid = BigDecimal.ZERO;
        List<Payment> payments = new ArrayList<>();
        
        for (PaymentRequest paymentRequest : request.getPayments()) {
            if (paymentRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Payment amount must be positive");
            }
            
            String paymentReference = paymentRequest.getPaymentReference();
            if (paymentReference == null || paymentReference.isEmpty()) {
                paymentReference = generatePaymentReference(paymentRequest.getMode());
            }
            
            Payment payment = Payment.builder()
                    .bill(bill)
                    .paymentReference(paymentReference)
                    .mode(paymentRequest.getMode())
                    .amount(paymentRequest.getAmount())
                    .status(Payment.PaymentStatus.COMPLETED)
                    .paymentDate(LocalDateTime.now())
                    .build();
            
            payments.add(payment);
            totalPaid = totalPaid.add(paymentRequest.getAmount());
        }
        
        // Validate payment amount (allow partial payments and overpayments)
        if (totalPaid.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Total payment amount must be greater than zero");
        }
        
        bill.setPayments(payments);
        
        // Update payment status based on amount paid
        // PARTIAL: paid < total amount
        // PAID: paid >= total amount (includes exact payment and overpayment)
        if (totalPaid.compareTo(bill.getTotalAmount()) < 0) {
            // Partially paid - amount is less than total
            bill.setPaymentStatus(Bill.PaymentStatus.PARTIALLY_PAID);
        } else {
            // Fully paid - amount equals or exceeds total (includes overpayment)
            bill.setPaymentStatus(Bill.PaymentStatus.PAID);
        }
        
        // Save bill (cascade saves items and payments)
        bill = billRepository.save(bill);
        
        // CRITICAL: Deduct stock ONLY after bill is saved and payment is confirmed
        // Stock deduction happens at batch level (quantity-based, not per-unit barcode)
        for (BillItem billItem : billItems) {
            // Deduct stock from batch
            batchService.deductStock(billItem.getBatch().getId(), billItem.getQuantity());
        }
        
        // Audit log
        auditService.log(AuditLog.ActionType.BILL_CREATED, cashier, "Bill", 
                        bill.getId().toString(), "Bill created: " + billNumber,
                        null, bill.toString(), httpRequest);
        
        return mapToResponse(bill);
    }
    
    @Transactional(readOnly = true)
    public BillResponse getBillById(Long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bill not found with id: " + id));
        return mapToResponse(bill);
    }
    
    @Transactional(readOnly = true)
    public BillResponse getBillByBillNumber(String billNumber) {
        Bill bill = billRepository.findByBillNumber(billNumber)
                .orElseThrow(() -> new RuntimeException("Bill not found with bill number: " + billNumber));
        return mapToResponse(bill);
    }
    
    @Transactional(readOnly = true)
    public List<BillResponse> getAllBills() {
        return billRepository.findAllOrderByBillDateDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void cancelBill(Long billId, String reason, User user, HttpServletRequest httpRequest) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found with id: " + billId));
        
        if (bill.getCancelled()) {
            throw new RuntimeException("Bill is already cancelled");
        }
        
        if (bill.getPaymentStatus() == Bill.PaymentStatus.PAID) {
            throw new RuntimeException("Cannot cancel a paid bill. Process a return instead.");
        }
        
        bill.setCancelled(true);
        bill.setCancellationReason(reason);
        billRepository.save(bill);
        
        // Restore stock for all items
        for (BillItem item : bill.getBillItems()) {
            batchService.restoreStock(item.getBatch().getId(), item.getQuantity());
        }
        
        auditService.log(AuditLog.ActionType.BILL_CANCELLED, user, "Bill", 
                        bill.getId().toString(), "Bill cancelled: " + reason,
                        null, bill.toString(), httpRequest);
    }
    
    private String generateBillNumber() {
        LocalDate today = LocalDate.now();
        String prefix = "BILL" + today.getYear() + String.format("%02d", today.getMonthValue()) + 
                       String.format("%02d", today.getDayOfMonth());
        
        try {
            Long maxSequence = billRepository.findMaxBillNumberSequence(prefix);
            long sequence = (maxSequence == null) ? 1 : maxSequence + 1;
            return prefix + String.format("%04d", sequence);
        } catch (Exception e) {
            // Fallback if query fails
            return prefix + String.format("%04d", System.currentTimeMillis() % 10000);
        }
    }
    
    private String generatePaymentReference(Payment.PaymentMode mode) {
        String prefix = mode.name().substring(0, 1);
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private BillResponse mapToResponse(Bill bill) {
        List<BillItemResponse> items = bill.getBillItems().stream()
                .map(this::mapItemToResponse)
                .collect(Collectors.toList());
        
        List<PaymentResponse> payments = bill.getPayments().stream()
                .map(this::mapPaymentToResponse)
                .collect(Collectors.toList());
        
        // Recalculate payment status based on actual payments
        // This ensures status is always correct even if payments were added later
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
     * PARTIALLY_PAID: totalPaid < totalAmount
     * PAID: totalPaid >= totalAmount (includes exact payment and overpayment)
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
}

