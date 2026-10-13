package com.tradejapan.part3.dto;

import com.tradejapan.part3.model.InquiryStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record InquiryView(
        Long inquiryId,
        Long userId,
        String customerName,
        String customerEmail,
        Long vehicleId,
        String vehicleName,
        String vehicleMake,
        String vehicleImage,
        BigDecimal vehiclePrice,
        String subject,
        String message,
        String destinationCountry,
        InquiryStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        long replyCount,            // replies written by the Trade Japan team
        long unreadCount,           // customer messages no admin has opened yet
        List<AdminRef> handledBy,   // admins who replied in this chat
        Long purchaseId
) {}
