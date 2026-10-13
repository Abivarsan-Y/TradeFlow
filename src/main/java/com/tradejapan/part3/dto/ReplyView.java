package com.tradejapan.part3.dto;

import java.time.LocalDateTime;

public record ReplyView(
        Long replyId,
        Long inquiryId,
        Long userId,
        String authorName,   // what the customer sees ("Trade Japan Team" or their own name)
        String staffName,    // the real admin's name (only used in the admin screens)
        boolean fromAdmin,
        String message,
        LocalDateTime createdAt
) {}
