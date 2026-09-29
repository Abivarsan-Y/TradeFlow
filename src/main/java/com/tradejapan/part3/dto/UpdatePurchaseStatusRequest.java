package com.tradejapan.part3.dto;

import com.tradejapan.part3.model.PurchaseStatus;
import jakarta.validation.constraints.NotNull;

public record UpdatePurchaseStatusRequest(
        @NotNull(message = "status is required") PurchaseStatus status
) {}
