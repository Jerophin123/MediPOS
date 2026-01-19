package com.medicalstore.pos.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ReturnRequest {
    @NotNull(message = "Bill ID is required")
    private Long billId;
    
    @NotBlank(message = "Reason is required")
    private String reason;
    
    @NotEmpty(message = "Return items cannot be empty")
    @Valid
    private List<ReturnItemRequest> items;
}





