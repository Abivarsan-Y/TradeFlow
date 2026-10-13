package com.tradejapan.part3.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateAdvancePaymentRequest(
        @NotNull(message = "advancePaid is required")
        @DecimalMin(value = "0.00", message = "advancePaid cannot be negative") BigDecimal advancePaid
) {}
