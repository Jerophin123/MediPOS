package com.medicalstore.pos.controller;

import com.medicalstore.pos.dto.request.CreateBillRequest;
import com.medicalstore.pos.dto.response.BillResponse;
import com.medicalstore.pos.entity.User;
import com.medicalstore.pos.service.BillingService;
import com.medicalstore.pos.service.PdfBillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/cashier/bills")
@Tag(name = "Billing", description = "Billing and POS APIs")
@SecurityRequirement(name = "bearerAuth")
public class BillingController {
    
    private final BillingService billingService;
    private final PdfBillService pdfBillService;
    
    public BillingController(BillingService billingService, PdfBillService pdfBillService) {
        this.billingService = billingService;
        this.pdfBillService = pdfBillService;
    }
    
    @PostMapping
    @Operation(summary = "Create bill", description = "Create a new bill with items and payments")
    public ResponseEntity<BillResponse> createBill(
            @Valid @RequestBody CreateBillRequest request,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpRequest) {
        BillResponse response = billingService.createBill(request, user, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get bill by ID", description = "Retrieve bill details by ID")
    public ResponseEntity<BillResponse> getBillById(@PathVariable Long id) {
        BillResponse response = billingService.getBillById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/number/{billNumber}")
    @Operation(summary = "Get bill by bill number", description = "Retrieve bill details by bill number")
    public ResponseEntity<BillResponse> getBillByBillNumber(@PathVariable String billNumber) {
        BillResponse response = billingService.getBillByBillNumber(billNumber);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(summary = "Get all bills", description = "Retrieve all bills ordered by date (purchase history)")
    public ResponseEntity<List<BillResponse>> getAllBills() {
        List<BillResponse> response = billingService.getAllBills();
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel bill", description = "Cancel an unpaid bill and restore stock")
    public ResponseEntity<Void> cancelBill(
            @PathVariable Long id,
            @RequestParam String reason,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpRequest) {
        billingService.cancelBill(id, reason, user, httpRequest);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/{id}/pdf")
    @Operation(summary = "Download bill PDF", description = "Generate and download bill as PDF")
    public ResponseEntity<byte[]> downloadBillPdf(@PathVariable Long id) {
        try {
            BillResponse bill = billingService.getBillById(id);
            byte[] pdfBytes = pdfBillService.generateBillPdf(bill);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Bill_" + bill.getBillNumber() + ".pdf");
            headers.setContentLength(pdfBytes.length);
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}


