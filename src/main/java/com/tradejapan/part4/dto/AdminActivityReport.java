package com.tradejapan.part4.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AdminActivityReport(List<AdminRow> admins, List<Event> recent) {
    public record AdminRow(Long adminId, String name, String email, String role,
                           long repliesSent, long purchasesProcessed,
                           long testimonialsReviewed, long contactMessagesHandled) {}

    public record Event(String type, String description, String adminName, LocalDateTime time) {}
}
