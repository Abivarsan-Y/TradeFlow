package com.tradejapan.orders.dto;

import com.tradejapan.orders.model.PurchaseStatus;
import jakarta.validation.constraints.NotNull;

public record UpdatePurchaseStatusRequest(
        @NotNull(message = "status is required") PurchaseStatus status
) {}
