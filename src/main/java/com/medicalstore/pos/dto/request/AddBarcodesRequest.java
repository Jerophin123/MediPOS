package com.medicalstore.pos.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AddBarcodesRequest {
    @NotEmpty(message = "Barcodes list cannot be empty")
    private List<String> barcodes;
}





