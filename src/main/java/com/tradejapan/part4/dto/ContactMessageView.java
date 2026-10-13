package com.tradejapan.part4.dto;

import com.tradejapan.part4.model.ContactStatus;

import java.time.LocalDateTime;

public record ContactMessageView(
        Long messageId,
        String name,
        String email,
        String subject,
        String message,
        ContactStatus status,
        LocalDateTime createdAt,
        String handledByName,
        LocalDateTime handledAt
) {}
