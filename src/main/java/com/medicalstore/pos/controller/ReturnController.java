package com.medicalstore.pos.controller;

import com.medicalstore.pos.dto.request.ReturnRequest;
import com.medicalstore.pos.dto.response.BillResponse;
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
}



