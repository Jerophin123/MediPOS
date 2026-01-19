package com.medicalstore.pos.controller;

import com.medicalstore.pos.dto.request.CreateMedicineRequest;
import com.medicalstore.pos.dto.request.UpdateMedicineRequest;
import com.medicalstore.pos.dto.response.MedicineResponse;
import com.medicalstore.pos.entity.User;
import com.medicalstore.pos.service.MedicineService;
import com.medicalstore.pos.service.StockBarcodeService;
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
@RequestMapping("/api/pharmacist/medicines")
@Tag(name = "Medicine Management", description = "Medicine management APIs")
@SecurityRequirement(name = "bearerAuth")
public class MedicineController {
    
    private final MedicineService medicineService;
    
    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }
    
    @PostMapping
    @Operation(summary = "Create medicine", description = "Add a new medicine to the system")
    public ResponseEntity<MedicineResponse> createMedicine(
            @Valid @RequestBody CreateMedicineRequest request,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpRequest) {
        MedicineResponse response = medicineService.createMedicine(request, user, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get medicine by ID", description = "Retrieve medicine details by ID")
    public ResponseEntity<MedicineResponse> getMedicineById(@PathVariable Long id) {
        MedicineResponse response = medicineService.getMedicineById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(summary = "Get all medicines", description = "Retrieve all medicines")
    public ResponseEntity<List<MedicineResponse>> getAllMedicines() {
        List<MedicineResponse> response = medicineService.getAllMedicines();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search medicines", description = "Search medicines by name or barcode")
    public ResponseEntity<List<MedicineResponse>> searchMedicines(@RequestParam String name) {
        List<MedicineResponse> response = medicineService.searchMedicines(name);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/barcode/{barcode}")
    @Operation(summary = "Find medicine by barcode", description = "Find medicine by GTIN/EAN barcode. Barcode identifies the product, not individual units.")
    public ResponseEntity<MedicineResponse> findMedicineByBarcode(@PathVariable String barcode) {
        MedicineResponse response = medicineService.findMedicineByBarcode(barcode);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/barcode/search")
    @Operation(summary = "Search medicines by barcode prefix", description = "Search medicines by GTIN/EAN barcode prefix for incremental search")
    public ResponseEntity<List<MedicineResponse>> searchMedicinesByBarcode(@RequestParam String prefix) {
        List<MedicineResponse> response = medicineService.searchMedicinesByBarcodePrefix(prefix);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/status")
    @Operation(summary = "Update medicine status", description = "Update medicine status (ACTIVE/DISCONTINUED)")
    public ResponseEntity<MedicineResponse> updateMedicineStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpRequest) {
        MedicineResponse response = medicineService.updateMedicineStatus(
                id, com.medicalstore.pos.entity.Medicine.Status.valueOf(status), user, httpRequest);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update medicine", description = "Update medicine details")
    public ResponseEntity<MedicineResponse> updateMedicine(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMedicineRequest request,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpRequest) {
        MedicineResponse response = medicineService.updateMedicine(id, request, user, httpRequest);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete medicine", description = "Delete a medicine (only if no batches exist)")
    public ResponseEntity<Void> deleteMedicine(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpRequest) {
        medicineService.deleteMedicine(id, user, httpRequest);
        return ResponseEntity.noContent().build();
    }
}

