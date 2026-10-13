package com.tradejapan.part3.dto;

import com.tradejapan.part3.model.PurchaseStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PurchaseView(
        Long purchaseId,
        Long userId,
        String customerName,
        String customerEmail,
        Long vehicleId,
        String vehicleName,
        String vehicleMake,
        String vehicleImage,
        Long processedBy,
        String processedByName,
        Long inquiryId,
        String destinationCountry,
        String invoiceNo,
        String invoiceFileUrl,
        BigDecimal totalPrice,
        BigDecimal advancePaid,
        BigDecimal balance,
        PurchaseStatus status,
        String notes,
        LocalDateTime createdAt
) {}
