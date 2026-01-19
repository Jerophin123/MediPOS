package com.medicalstore.pos.controller;

import com.medicalstore.pos.dto.response.GstReportResponse;
import com.medicalstore.pos.dto.response.SalesReportResponse;
import com.medicalstore.pos.dto.response.StockReportResponse;
import com.medicalstore.pos.service.ReportingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/reports")
@Tag(name = "Reports", description = "Reporting and analytics APIs")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {
    
    private final ReportingService reportingService;
    
    public ReportController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }
    
    @GetMapping("/sales")
    @Operation(summary = "Get sales report", description = "Get daily sales report for a date range")
    public ResponseEntity<SalesReportResponse> getSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        SalesReportResponse response = reportingService.getDailySalesReport(startDate, endDate);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/gst")
    @Operation(summary = "Get GST report", description = "Get GST report with CGST/SGST breakup")
    public ResponseEntity<GstReportResponse> getGstReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        GstReportResponse response = reportingService.getGstReport(startDate, endDate);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/cashier/{cashierId}")
    @Operation(summary = "Get cashier sales report", description = "Get sales report for a specific cashier")
    public ResponseEntity<SalesReportResponse> getCashierSalesReport(
            @PathVariable Long cashierId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        SalesReportResponse response = reportingService.getCashierSalesReport(cashierId, startDate, endDate);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/stock")
    @Operation(summary = "Get stock report", description = "Get comprehensive stock report with medicine-wise inventory")
    public ResponseEntity<StockReportResponse> getStockReport() {
        StockReportResponse response = reportingService.getStockReport();
        return ResponseEntity.ok(response);
    }
}

