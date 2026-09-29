package com.tradejapan.part3.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreatePurchaseRequest(
        @NotNull(message = "userId is required") Long userId,
        @NotNull(message = "vehicleId is required") Long vehicleId,
        @NotNull(message = "processedBy is required") Long processedBy,
        Long inquiryId,
        @NotNull(message = "totalPrice is required")
        @DecimalMin(value = "0.01", message = "totalPrice must be greater than 0") BigDecimal totalPrice,
        @DecimalMin(value = "0.00", message = "advancePaid cannot be negative") BigDecimal advancePaid,
        @Size(max = 2000, message = "notes must be 2000 characters or less") String notes
) {}
