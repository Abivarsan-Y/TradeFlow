package com.tradejapan.part3.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Either link an inquiry (buyer, vehicle and destination are taken from it),
 * or give buyerEmail + vehicleId (+ optional destinationCountry) for a direct purchase.
 * processedBy is always the logged-in admin.
 */
public record CreatePurchaseRequest(
        Long inquiryId,
        String buyerEmail,
        Long vehicleId,
        @Size(max = 100, message = "Destination country must be 100 characters or less") String destinationCountry,
        @NotNull(message = "Total price is required")
        @DecimalMin(value = "0.01", message = "Total price must be greater than 0") BigDecimal totalPrice,
        @DecimalMin(value = "0.00", message = "Advance paid cannot be negative") BigDecimal advancePaid,
        @Size(max = 2000, message = "Notes must be 2000 characters or less") String notes
) {}
