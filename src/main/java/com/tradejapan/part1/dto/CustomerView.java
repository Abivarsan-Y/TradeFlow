package com.tradejapan.cms.dto;

import java.time.LocalDateTime;

/** One row of the admin "Customers" list. Passwords and tokens are never included. */
public record CustomerView(
        Long id,
        String firstName,
        String lastName,
        String email,
        String whatsappNo,
        String status,          // ACTIVE / INACTIVE
        String authProvider,    // LOCAL / GOOGLE
        LocalDateTime createdAt,
        long inquiryCount,
        long purchaseCount
) {}
