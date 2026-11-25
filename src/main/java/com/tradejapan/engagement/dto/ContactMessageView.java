package com.tradejapan.engagement.dto;

import com.tradejapan.engagement.model.ContactStatus;

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
