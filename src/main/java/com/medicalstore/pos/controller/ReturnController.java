package com.medicalstore.pos.controller;

import com.medicalstore.pos.dto.request.ReturnRequest;
import com.medicalstore.pos.dto.response.BillResponse;
import com.medicalstore.pos.dto.response.ReturnResponse;
import com.medicalstore.pos.entity.User;
import com.medicalstore.pos.service.ReturnService;
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
@RequestMapping("/api/cashier/returns")
@Tag(name = "Returns & Refunds", description = "Return and refund processing APIs")
@SecurityRequirement(name = "bearerAuth")
public class ReturnController {
    
    private final ReturnService returnService;
    
    public ReturnController(ReturnService returnService) {
        this.returnService = returnService;
    }
    
    @PostMapping
    @Operation(summary = "Process return", description = "Process a return and restore stock to original batch")
    public ResponseEntity<BillResponse> processReturn(
            @Valid @RequestBody ReturnRequest request,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpRequest) {
        BillResponse response = returnService.processReturn(request, user, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    @Operation(summary = "Get all returns", description = "Get a list of all processed returns")
    public ResponseEntity<List<ReturnResponse>> getAllReturns() {
        List<ReturnResponse> returns = returnService.getAllReturns();
        return ResponseEntity.ok(returns);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get return by ID", description = "Get return details by return ID")
    public ResponseEntity<ReturnResponse> getReturnById(@PathVariable Long id) {
        ReturnResponse returnResponse = returnService.getReturnById(id);
        return ResponseEntity.ok(returnResponse);
    }
    
    @GetMapping("/bill/{billId}")
    @Operation(summary = "Get returns by bill ID", description = "Get all returns for a specific bill")
    public ResponseEntity<List<ReturnResponse>> getReturnsByBillId(@PathVariable Long billId) {
        List<ReturnResponse> returns = returnService.getReturnsByBillId(billId);
        return ResponseEntity.ok(returns);
    }
}





