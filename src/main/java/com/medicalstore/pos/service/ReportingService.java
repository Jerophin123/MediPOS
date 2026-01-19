package com.medicalstore.pos.service;

import com.medicalstore.pos.dto.response.GstReportResponse;
import com.medicalstore.pos.dto.response.SalesReportResponse;
import com.medicalstore.pos.dto.response.StockReportResponse;
import com.medicalstore.pos.entity.Batch;
import com.medicalstore.pos.entity.Bill;
import com.medicalstore.pos.entity.BillItem;
import com.medicalstore.pos.entity.Medicine;
import com.medicalstore.pos.entity.Payment;
import com.medicalstore.pos.repository.BatchRepository;
import com.medicalstore.pos.repository.BillRepository;
import com.medicalstore.pos.repository.MedicineRepository;
import com.medicalstore.pos.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportingService {
    
    private final BillRepository billRepository;
    private final PaymentRepository paymentRepository;
    private final MedicineRepository medicineRepository;
    private final BatchRepository batchRepository;
    private static final int LOW_STOCK_THRESHOLD = 10;
    
    public ReportingService(BillRepository billRepository, 
                           PaymentRepository paymentRepository,
                           MedicineRepository medicineRepository,
                           BatchRepository batchRepository) {
        this.billRepository = billRepository;
        this.paymentRepository = paymentRepository;
        this.medicineRepository = medicineRepository;
        this.batchRepository = batchRepository;
    }
    
    @Transactional(readOnly = true)
    public SalesReportResponse getDailySalesReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        
        List<Bill> bills = billRepository.findBillsByDateRange(start, end);
        bills = bills.stream()
                .filter(bill -> !bill.getCancelled())
                .collect(Collectors.toList());
        
        BigDecimal totalSales = bills.stream()
                .map(Bill::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalGst = bills.stream()
                .map(Bill::getTotalGst)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Get payments by mode
        List<Payment> payments = paymentRepository.findPaymentsByDateRange(start, end);
        payments = payments.stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.COMPLETED)
                .collect(Collectors.toList());
        
        BigDecimal totalCash = payments.stream()
                .filter(p -> p.getMode() == Payment.PaymentMode.CASH)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalUpi = payments.stream()
                .filter(p -> p.getMode() == Payment.PaymentMode.UPI)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalCard = payments.stream()
                .filter(p -> p.getMode() == Payment.PaymentMode.CARD)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Daily breakdown
        Map<LocalDate, List<Bill>> billsByDate = bills.stream()
                .collect(Collectors.groupingBy(bill -> bill.getBillDate().toLocalDate()));
        
        List<SalesReportResponse.DailySales> dailySales = billsByDate.entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<Bill> dayBills = entry.getValue();
                    BigDecimal dayTotal = dayBills.stream()
                            .map(Bill::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    return SalesReportResponse.DailySales.builder()
                            .date(date)
                            .billCount(dayBills.size())
                            .totalAmount(dayTotal)
                            .build();
                })
                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                .collect(Collectors.toList());
        
        return SalesReportResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalBills(bills.size())
                .totalSales(totalSales)
                .totalGst(totalGst)
                .totalCash(totalCash)
                .totalUpi(totalUpi)
                .totalCard(totalCard)
                .dailySales(dailySales)
                .build();
    }
    
    @Transactional(readOnly = true)
    public GstReportResponse getGstReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        
        List<Bill> bills = billRepository.findBillsByDateRange(start, end);
        bills = bills.stream()
                .filter(bill -> !bill.getCancelled())
                .collect(Collectors.toList());
        
        // Group by HSN code
        Map<String, List<BillItem>> itemsByHsn = bills.stream()
                .flatMap(bill -> bill.getBillItems().stream())
                .collect(Collectors.groupingBy(item -> item.getMedicine().getHsnCode()));
        
        List<GstReportResponse.GstBreakup> gstBreakup = new ArrayList<>();
        BigDecimal totalCgst = BigDecimal.ZERO;
        BigDecimal totalSgst = BigDecimal.ZERO;
        
        for (Map.Entry<String, List<BillItem>> entry : itemsByHsn.entrySet()) {
            String hsnCode = entry.getKey();
            List<BillItem> items = entry.getValue();
            
            if (items.isEmpty()) continue;
            
            BillItem firstItem = items.get(0);
            String medicineName = firstItem.getMedicine().getName();
            BigDecimal gstPercentage = firstItem.getGstPercentage();
            
            BigDecimal taxableAmount = items.stream()
                    .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal totalGst = items.stream()
                    .map(BillItem::getGstAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Split GST into CGST and SGST (assuming 50-50 split for India)
            BigDecimal cgst = totalGst.divide(BigDecimal.valueOf(2), 2, java.math.RoundingMode.HALF_UP);
            BigDecimal sgst = totalGst.subtract(cgst);
            
            totalCgst = totalCgst.add(cgst);
            totalSgst = totalSgst.add(sgst);
            
            gstBreakup.add(GstReportResponse.GstBreakup.builder()
                    .hsnCode(hsnCode)
                    .medicineName(medicineName)
                    .gstPercentage(gstPercentage)
                    .taxableAmount(taxableAmount)
                    .cgst(cgst)
                    .sgst(sgst)
                    .totalGst(totalGst)
                    .build());
        }
        
        return GstReportResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalCgst(totalCgst)
                .totalSgst(totalSgst)
                .totalGst(totalCgst.add(totalSgst))
                .gstBreakup(gstBreakup)
                .build();
    }
    
    @Transactional(readOnly = true)
    public SalesReportResponse getCashierSalesReport(Long cashierId, LocalDate startDate, LocalDate endDate) {
        // This would require a User entity lookup - simplified for now
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        
        // Note: This requires User entity - would need to fetch user first
        // For now, returning empty report
        return SalesReportResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalBills(0)
                .totalSales(BigDecimal.ZERO)
                .totalGst(BigDecimal.ZERO)
                .totalCash(BigDecimal.ZERO)
                .totalUpi(BigDecimal.ZERO)
                .totalCard(BigDecimal.ZERO)
                .dailySales(new ArrayList<>())
                .build();
    }
    
    @Transactional(readOnly = true)
    public StockReportResponse getStockReport() {
        LocalDate today = LocalDate.now();
        List<Medicine> medicines = medicineRepository.findAll();
        List<Batch> allBatches = batchRepository.findAll();
        
        int totalMedicines = medicines.size();
        int totalBatches = allBatches.size();
        int totalStockQuantity = 0;
        int availableStockQuantity = 0;
        int expiredStockQuantity = 0;
        int lowStockMedicines = 0;
        int outOfStockMedicines = 0;
        BigDecimal totalStockValue = BigDecimal.ZERO;
        
        List<StockReportResponse.MedicineStockItem> medicineStock = new ArrayList<>();
        List<StockReportResponse.ExpiredStockItem> expiredStock = new ArrayList<>();
        List<StockReportResponse.LowStockItem> lowStockItems = new ArrayList<>();
        
        // Process each medicine
        for (Medicine medicine : medicines) {
            List<Batch> medicineBatches = batchRepository.findByMedicine(medicine);
            
            int medicineTotalStock = batchRepository.getTotalStockQuantity(medicine);
            int medicineAvailableStock = batchRepository.getTotalAvailableQuantity(medicine, today);
            int medicineExpiredStock = medicineTotalStock - medicineAvailableStock;
            
            totalStockQuantity += medicineTotalStock;
            availableStockQuantity += medicineAvailableStock;
            expiredStockQuantity += medicineExpiredStock;
            
            boolean isLowStock = medicineAvailableStock > 0 && medicineAvailableStock <= LOW_STOCK_THRESHOLD;
            boolean isOutOfStock = medicineAvailableStock == 0;
            
            if (isLowStock) {
                lowStockMedicines++;
            }
            if (isOutOfStock) {
                outOfStockMedicines++;
            }
            
            // Calculate average selling price and stock value
            BigDecimal avgSellingPrice = BigDecimal.ZERO;
            BigDecimal medicineStockValue = BigDecimal.ZERO;
            
            if (!medicineBatches.isEmpty()) {
                BigDecimal totalSellingPrice = medicineBatches.stream()
                        .map(Batch::getSellingPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                avgSellingPrice = totalSellingPrice.divide(
                        BigDecimal.valueOf(medicineBatches.size()), 2, RoundingMode.HALF_UP);
                
                // Calculate stock value using purchase price (cost basis)
                for (Batch batch : medicineBatches) {
                    if (batch.getExpiryDate().isAfter(today) || batch.getExpiryDate().isEqual(today)) {
                        medicineStockValue = medicineStockValue.add(
                                batch.getPurchasePrice().multiply(BigDecimal.valueOf(batch.getQuantityAvailable())));
                    }
                }
            }
            
            totalStockValue = totalStockValue.add(medicineStockValue);
            
            medicineStock.add(StockReportResponse.MedicineStockItem.builder()
                    .medicineId(medicine.getId())
                    .medicineName(medicine.getName())
                    .manufacturer(medicine.getManufacturer())
                    .category(medicine.getCategory())
                    .hsnCode(medicine.getHsnCode())
                    .totalStock(medicineTotalStock)
                    .availableStock(medicineAvailableStock)
                    .expiredStock(medicineExpiredStock)
                    .lowStock(isLowStock)
                    .outOfStock(isOutOfStock)
                    .averageSellingPrice(avgSellingPrice)
                    .stockValue(medicineStockValue)
                    .build());
            
            // Add to low stock items if applicable
            if (isLowStock && !isOutOfStock) {
                lowStockItems.add(StockReportResponse.LowStockItem.builder()
                        .medicineId(medicine.getId())
                        .medicineName(medicine.getName())
                        .manufacturer(medicine.getManufacturer())
                        .availableStock(medicineAvailableStock)
                        .lowStockThreshold(LOW_STOCK_THRESHOLD)
                        .averageSellingPrice(avgSellingPrice)
                        .build());
            }
        }
        
        // Get expired batches
        List<Batch> expiredBatches = batchRepository.findExpiredBatches(today);
        for (Batch batch : expiredBatches) {
            if (batch.getQuantityAvailable() > 0) {
                expiredStock.add(StockReportResponse.ExpiredStockItem.builder()
                        .batchId(batch.getId())
                        .medicineId(batch.getMedicine().getId())
                        .medicineName(batch.getMedicine().getName())
                        .batchNumber(batch.getBatchNumber())
                        .expiryDate(batch.getExpiryDate())
                        .quantity(batch.getQuantityAvailable())
                        .purchasePrice(batch.getPurchasePrice())
                        .stockValue(batch.getPurchasePrice().multiply(
                                BigDecimal.valueOf(batch.getQuantityAvailable())))
                        .build());
            }
        }
        
        // Sort medicine stock by name
        medicineStock.sort((a, b) -> a.getMedicineName().compareToIgnoreCase(b.getMedicineName()));
        
        return StockReportResponse.builder()
                .reportDate(today)
                .totalMedicines(totalMedicines)
                .totalBatches(totalBatches)
                .totalStockQuantity(totalStockQuantity)
                .availableStockQuantity(availableStockQuantity)
                .expiredStockQuantity(expiredStockQuantity)
                .lowStockMedicines(lowStockMedicines)
                .outOfStockMedicines(outOfStockMedicines)
                .totalStockValue(totalStockValue)
                .medicineStock(medicineStock)
                .expiredStock(expiredStock)
                .lowStockItems(lowStockItems)
                .build();
    }
}

